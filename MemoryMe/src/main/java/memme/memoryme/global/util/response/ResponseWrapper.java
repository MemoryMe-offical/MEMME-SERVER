package memme.memoryme.global.util.response;

import java.time.LocalDateTime;

public record ResponseWrapper<T>(
        boolean success,
        int status,
        String message,
        LocalDateTime timestamp,
        T data
) {
    public static <T> ResponseWrapper<T> ok(int status, String message, T data) {
        return new ResponseWrapper<>(
                true,
                status,
                message,
                LocalDateTime.now(),
                data
        );
    }

    public static <T> ResponseWrapper<T> fail(int status, String message, T data) {
        return new ResponseWrapper<>(
                false,
                status,
                message,
                LocalDateTime.now(),
                data
        );
    }

    public static ResponseWrapper<Void> fail(int status, String message) {
        return new ResponseWrapper<>(
                false,
                status,
                message,
                LocalDateTime.now(),
                null
        );
    }
}