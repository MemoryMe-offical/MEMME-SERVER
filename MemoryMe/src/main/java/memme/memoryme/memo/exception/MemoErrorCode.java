package memme.memoryme.memo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemoErrorCode implements BaseErrorCode {
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO_001", "메모를 찾을 수 없습니다."),
    MEMO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEMO_002", "해당 메모에 접근할 수 없습니다."),
    INVALID_MEMO_REQUEST(HttpStatus.BAD_REQUEST, "MEMO_003", "유효하지 않은 메모 요청입니다."),
    USER_UID_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO_004", "해당 사용자 UID를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
