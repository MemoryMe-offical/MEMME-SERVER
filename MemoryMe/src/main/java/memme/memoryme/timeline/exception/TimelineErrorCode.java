package memme.memoryme.timeline.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TimelineErrorCode implements BaseErrorCode {
    INVALID_TIMELINE_REQUEST(HttpStatus.BAD_REQUEST, "TIMELINE_001", "유효하지 않은 타임라인 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
