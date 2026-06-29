package memme.memoryme.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}