package memme.memoryme.og.application.service;

import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.og.exception.OgErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OgService {
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final int MAX_TITLE_LENGTH = 180;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private final OpenAiSummaryService openAiSummaryService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public OgService(OpenAiSummaryService openAiSummaryService) {
        this.openAiSummaryService = openAiSummaryService;
    }

    public OgDataDto fetch(String url) {
        if (!isHttpUrl(url)) {
            throw new BusinessException(OgErrorCode.INVALID_OG_REQUEST);
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

            return new OgDataDto(title, description, imageUrl, siteName, null);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    public OgDataDto fetchWithSummary(String url) {
        OgDataDto ogData = fetch(url);
        if (ogData == null) {
            throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
        }
        String summary = openAiSummaryService.summarize(url, ogData);
        return new OgDataDto(
                ogData.title(),
                ogData.description(),
                ogData.imageUrl(),
                ogData.siteName(),
                summary
        );
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
}
