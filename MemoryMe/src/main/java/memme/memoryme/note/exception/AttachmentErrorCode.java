package memme.memoryme.note.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AttachmentErrorCode implements BaseErrorCode {
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTACHMENT_001", "첨부파일을 찾을 수 없습니다."),
    INVALID_ATTACHMENT_REQUEST(HttpStatus.BAD_REQUEST, "ATTACHMENT_002", "유효하지 않은 첨부파일 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
