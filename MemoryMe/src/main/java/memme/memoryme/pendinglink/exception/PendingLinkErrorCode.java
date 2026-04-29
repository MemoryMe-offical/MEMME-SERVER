package memme.memoryme.pendinglink.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PendingLinkErrorCode implements BaseErrorCode {
    PENDING_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "PENDING_LINK_001", "대기 중인 링크를 찾을 수 없습니다."),
    INVALID_PENDING_LINK_REQUEST(HttpStatus.BAD_REQUEST, "PENDING_LINK_002", "유효하지 않은 링크 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
