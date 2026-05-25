package memme.memoryme.og.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.og.exception.OgErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OgService {
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final int MAX_TITLE_LENGTH = 180;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private final OpenAiSummaryService openAiSummaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public OgService(OpenAiSummaryService openAiSummaryService) {
        this.openAiSummaryService = openAiSummaryService;
    }

    public OgDataDto fetch(String url) {
        OgFetchResult result = fetchResult(url);
        return result == null ? null : result.ogData();
    }

    public OgDataDto fetchWithSummary(String url) {
        OgFetchResult result = fetchResult(url);
        if (result == null || result.ogData() == null) {
            throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
        }
        OgDataDto ogData = result.ogData();
        String summary = openAiSummaryService.summarize(url, ogData, result.sourceText());
        return new OgDataDto(
                ogData.title(),
                ogData.description(),
                ogData.imageUrl(),
                ogData.siteName(),
                summary
        );
    }

    private OgFetchResult fetchResult(String url) {
        if (!isHttpUrl(url)) {
            throw new BusinessException(OgErrorCode.INVALID_OG_REQUEST);
        }

        OgFetchResult youtubeResult = fetchYoutubeOEmbed(url);
        if (youtubeResult != null) {
            return youtubeResult;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url.trim()))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "MemoryMeBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 400) {
                return null;
            }

            String html = response.body();
            String title = firstNonBlank(
                    meta(html, "property", "og:title"),
                    meta(html, "name", "twitter:title"),
                    title(html)
            );
            String description = firstNonBlank(
                    meta(html, "property", "og:description"),
                    meta(html, "name", "description"),
                    meta(html, "name", "twitter:description")
            );
            String imageUrl = firstNonBlank(
                    meta(html, "property", "og:image"),
                    meta(html, "name", "twitter:image")
            );
            String siteName = meta(html, "property", "og:site_name");
            title = normalizeTitle(title, siteName);
            description = normalizeDescription(description, siteName);
            String sourceText = extractSourceText(html, title, description, siteName);

            return new OgFetchResult(new OgDataDto(title, description, imageUrl, siteName, null), sourceText);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private OgFetchResult fetchYoutubeOEmbed(String url) {
        if (!isYoutubeUrl(url)) {
            return null;
        }
        try {
            String encodedUrl = URLEncoder.encode(url.trim(), StandardCharsets.UTF_8);
            URI uri = URI.create("https://www.youtube.com/oembed?format=json&url=" + encodedUrl);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "MemoryMeBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            String title = cleanText(root.path("title").asText(null));
            String author = cleanText(root.path("author_name").asText(null));
            String thumbnailUrl = cleanText(root.path("thumbnail_url").asText(null));
            if (title == null) {
                return null;
            }

            String description = author == null ? null : "채널: " + author;
            String sourceText = youtubeSourceText(url, title, author);
            return new OgFetchResult(
                    new OgDataDto(
                            abbreviate(title, MAX_TITLE_LENGTH),
                            description,
                            thumbnailUrl,
                            "YouTube",
                            null
                    ),
                    sourceText
            );
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private boolean isYoutubeUrl(String url) {
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            return normalized.equals("youtu.be")
                    || normalized.endsWith(".youtube.com")
                    || normalized.equals("youtube.com")
                    || normalized.endsWith(".youtube-nocookie.com")
                    || normalized.equals("youtube-nocookie.com");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String youtubeSourceText(String url, String title, String author) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "site", "YouTube");
        appendLine(builder, "type", "video");
        appendLine(builder, "url", url);
        appendLine(builder, "title", title);
        appendLine(builder, "channel", author);
        return builder.toString().trim();
    }

    private boolean isHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String meta(String html, String attributeName, String attributeValue) {
        String regex = "<meta[^>]*" + attributeName + "=[\\\"']" + Pattern.quote(attributeValue) + "[\\\"'][^>]*content=[\\\"']([^\\\"']*)[\\\"'][^>]*>"
                + "|<meta[^>]*content=[\\\"']([^\\\"']*)[\\\"'][^>]*" + attributeName + "=[\\\"']" + Pattern.quote(attributeValue) + "[\\\"'][^>]*>";
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        return decode(value);
    }

    private String title(String html) {
        Matcher matcher = TITLE_PATTERN.matcher(html);
        return matcher.find() ? decode(matcher.group(1)) : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return cleanText(value);
            }
        }
        return null;
    }

    private String decode(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlUnescape(value).trim();
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = HtmlUtils.htmlUnescape(value)
                .replaceAll("<[^>]+>", " ")
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private String normalizeTitle(String title, String siteName) {
        String cleaned = cleanText(title);
        if (cleaned == null) {
            return null;
        }
        if ("Instagram".equalsIgnoreCase(siteName)) {
            int markerIndex = cleaned.indexOf(" on Instagram:");
            if (markerIndex > 0) {
                return cleaned.substring(0, markerIndex) + " on Instagram";
            }
        }
        return abbreviate(cleaned, MAX_TITLE_LENGTH);
    }

    private String normalizeDescription(String description, String siteName) {
        String cleaned = cleanText(description);
        if (cleaned == null) {
            return null;
        }
        if ("Instagram".equalsIgnoreCase(siteName)) {
            cleaned = extractInstagramCaption(cleaned);
        }
        return abbreviate(cleaned, MAX_DESCRIPTION_LENGTH);
    }

    private String extractSourceText(String html, String title, String description, String siteName) {
        String author = firstNonBlank(
                meta(html, "name", "author"),
                meta(html, "property", "article:author")
        );
        String publishedAt = firstNonBlank(
                meta(html, "property", "article:published_time"),
                meta(html, "name", "date")
        );
        String bodyText = firstNonBlank(
                extractTagText(html, "article"),
                extractTagText(html, "main"),
                extractBodyText(html)
        );

        StringBuilder builder = new StringBuilder();
        appendLine(builder, "site", siteName);
        appendLine(builder, "author", author);
        appendLine(builder, "publishedAt", publishedAt);
        appendLine(builder, "title", title);
        appendLine(builder, "description", description);
        if (bodyText != null && !isSimilar(bodyText, description)) {
            appendLine(builder, "pageText", abbreviate(bodyText, 5000));
        }
        return builder.toString().trim();
    }

    private String extractTagText(String html, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\b[^>]*>(.*?)</" + tagName + ">", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        String longest = null;
        while (matcher.find()) {
            String text = stripHtml(matcher.group(1));
            if (text != null && (longest == null || text.length() > longest.length())) {
                longest = text;
            }
        }
        return longest;
    }

    private String extractBodyText(String html) {
        Matcher matcher = Pattern.compile("<body\\b[^>]*>(.*?)</body>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        return matcher.find() ? stripHtml(matcher.group(1)) : null;
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        String stripped = html
                .replaceAll("(?is)<script\\b[^>]*>.*?</script>", " ")
                .replaceAll("(?is)<style\\b[^>]*>.*?</style>", " ")
                .replaceAll("(?is)<noscript\\b[^>]*>.*?</noscript>", " ")
                .replaceAll("(?is)<svg\\b[^>]*>.*?</svg>", " ")
                .replaceAll("(?is)<header\\b[^>]*>.*?</header>", " ")
                .replaceAll("(?is)<footer\\b[^>]*>.*?</footer>", " ")
                .replaceAll("(?is)<nav\\b[^>]*>.*?</nav>", " ")
                .replaceAll("<[^>]+>", " ");
        return cleanText(stripped);
    }

    private void appendLine(StringBuilder builder, String label, String value) {
        String cleaned = cleanText(value);
        if (cleaned != null) {
            builder.append(label).append(": ").append(cleaned).append('\n');
        }
    }

    private boolean isSimilar(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        List<String> leftTokens = List.of(left.toLowerCase().split("\\s+"));
        List<String> rightTokens = List.of(right.toLowerCase().split("\\s+"));
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return false;
        }
        long overlap = rightTokens.stream().filter(leftTokens::contains).count();
        return overlap >= Math.min(20, rightTokens.size() * 0.8);
    }

    private String extractInstagramCaption(String description) {
        Matcher matcher = Pattern.compile(".*?:\\s*\"(.*)\"\\.?$", Pattern.DOTALL).matcher(description);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return description;
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)).trim() + "...";
    }

    private record OgFetchResult(OgDataDto ogData, String sourceText) {
    }
}
