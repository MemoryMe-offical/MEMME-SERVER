package memme.memoryme.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_L001", "이미 가입된 이메일입니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_L002", "인증 요청이 존재하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_L003", "이메일 인증이 완료되지 않았습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_L004", "사용자가 존재하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_L005", "비밀번호가 올바르지 않습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_T001", "유효하지 않은 토큰입니다."),
    NOT_A_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_T002", "Refresh 토큰이 아닙니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_T003", "토큰 정보가 일치하지 않습니다."),

    KAKAO_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "AUTH_K001", "카카오 Access Token 발급에 실패했습니다."),
    KAKAO_USER_PROFILE_FAILED(HttpStatus.BAD_REQUEST, "AUTH_K002", "카카오 이메일 정보를 가져올 수 없습니다."),
    KAKAO_API_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_K999", "카카오 API 통신 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
