package memme.memoryme.og.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.og.config.OpenAiSummaryProperties;
import memme.memoryme.og.exception.OgErrorCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiSummaryService {
    private static final URI CHAT_COMPLETIONS_URI = URI.create("https://api.openai.com/v1/chat/completions");
    private static final int MAX_INPUT_LENGTH = 6000;

    private final OpenAiSummaryProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public String summarize(String url, OgDataDto ogData) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
        }
        if (ogData == null || isBlank(ogData.title(), ogData.description(), ogData.siteName())) {
            throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
        }

        try {
            String body = objectMapper.writeValueAsString(requestBody(url, ogData));
            HttpRequest request = HttpRequest.newBuilder(CHAT_COMPLETIONS_URI)
                    .timeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())))
                    .header("Authorization", "Bearer " + properties.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
            }
            return extractSummary(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
        }
    }

    private Map<String, Object> requestBody(String url, OgDataDto ogData) {
        return Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "너는 링크 미리보기 데이터를 바탕으로 한국어 요약을 만든다. 입력에 없는 사실은 만들지 말고, 앱 화면에 바로 보여줄 요약 문장만 반환해라."
                        ),
                        Map.of(
                                "role", "user",
                                "content", input(url, ogData)
                        )
                ),
                "temperature", 0.2,
                "max_tokens", Math.max(100, properties.getMaxTokens())
        );
    }

    private String input(String url, OgDataDto ogData) {
        String value = """
                다음 링크 메타데이터를 한국어 1~2문장으로 자연스럽게 요약해줘.
                제목, 키워드, 불릿, 접두사 없이 요약문만 반환해줘.
                소셜 게시물이라면 좋아요/댓글 수보다 실제 게시물 내용 중심으로 요약해줘.

                url: %s
                title: %s
                description: %s
                siteName: %s
                """.formatted(
                nullToEmpty(url),
                nullToEmpty(ogData.title()),
                nullToEmpty(ogData.description()),
                nullToEmpty(ogData.siteName())
        );
        return value.length() > MAX_INPUT_LENGTH ? value.substring(0, MAX_INPUT_LENGTH) : value;
    }

    private String extractSummary(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank()) {
            throw new BusinessException(OgErrorCode.AI_SUMMARY_UNAVAILABLE);
        }
        return content.asText().trim();
    }

    private boolean isBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
