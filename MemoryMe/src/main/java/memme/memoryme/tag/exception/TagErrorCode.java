package memme.memoryme.tag.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TagErrorCode implements BaseErrorCode {
    INVALID_TAG_REQUEST(HttpStatus.BAD_REQUEST, "TAG_001", "유효하지 않은 태그 조회 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
