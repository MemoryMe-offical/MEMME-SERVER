package memme.memoryme.global.util.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "공통 응답 래퍼")
public class ResponseWrapper<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;

    @Schema(description = "응답 메시지", example = "요청 성공")
    private String message;

    @Schema(description = "응답 시각", example = "2026-03-20T16:10:00")
    private LocalDateTime timestamp;

    @Schema(description = "실제 응답 데이터")
    private T data;

    public static <T> ResponseWrapper<T> ok(int status, String message, T data) {
        return new ResponseWrapper<>(
                true,
                status,
                message,
                LocalDateTime.now(),
                data
        );
    }

    public static <T> ResponseWrapper<T> fail(int status, String message, T data) {
        return new ResponseWrapper<>(
                false,
                status,
                message,
                LocalDateTime.now(),
                data
        );
    }

    public static ResponseWrapper<Void> fail(int status, String message) {
        return new ResponseWrapper<>(
                false,
                status,
                message,
                LocalDateTime.now(),
                null
        );
    }
}