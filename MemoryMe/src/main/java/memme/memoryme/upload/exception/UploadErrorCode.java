package memme.memoryme.upload.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UploadErrorCode implements BaseErrorCode {
    INVALID_UPLOAD_REQUEST(HttpStatus.BAD_REQUEST, "UPLOAD_001", "유효하지 않은 업로드 요청입니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "UPLOAD_002", "지원하지 않는 파일 형식입니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_003", "파일 업로드에 실패했습니다."),
    INVALID_OBJECT_KEY(HttpStatus.BAD_REQUEST, "UPLOAD_004", "유효하지 않은 객체 키입니다."),
    OBJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "UPLOAD_005", "해당 객체에 접근할 수 없습니다."),
    OBJECT_LIST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_006", "업로드 객체 목록 조회에 실패했습니다."),
    OBJECT_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_007", "업로드 객체 삭제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
