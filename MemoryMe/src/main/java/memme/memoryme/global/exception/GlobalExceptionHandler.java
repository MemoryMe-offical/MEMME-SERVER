package memme.memoryme.global.exception;

import memme.memoryme.global.util.response.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseWrapper<ErrorDetail>> handleCustomException(BusinessException e) {
        BaseErrorCode errorCode = e.getErrorCode();

        ErrorDetail errorDetail = new ErrorDetail(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ResponseWrapper.fail(
                        errorCode.getHttpStatus().value(),
                        errorCode.getMessage(),
                        errorDetail
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<ErrorDetail>> handleException(Exception e) {
        ErrorDetail errorDetail = new ErrorDetail(
                CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ResponseWrapper.fail(
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value(),
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                        errorDetail
                ));
    }
}