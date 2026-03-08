package memme.memoryme.global.util.response;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class ResponseWrapper<T> {
    boolean success;
    int status;
    String message;
    LocalDateTime timestamp;
    T data;
}
