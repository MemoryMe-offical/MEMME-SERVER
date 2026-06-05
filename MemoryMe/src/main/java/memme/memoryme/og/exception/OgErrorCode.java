package memme.memoryme.og.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OgErrorCode implements BaseErrorCode {
    INVALID_OG_REQUEST(HttpStatus.BAD_REQUEST, "OG_001", "유효하지 않은 OG 조회 요청입니다."),
    AI_SUMMARY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "OG_002", "AI 요약을 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
