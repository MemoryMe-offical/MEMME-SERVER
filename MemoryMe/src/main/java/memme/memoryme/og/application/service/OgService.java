package memme.memoryme.og.application.service;

import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.og.exception.OgErrorCode;
import org.springframework.stereotype.Service;

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
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

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

            return new OgDataDto(title, description, imageUrl, siteName);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
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
                return value.trim();
            }
        }
        return null;
    }

    private String decode(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }
}
