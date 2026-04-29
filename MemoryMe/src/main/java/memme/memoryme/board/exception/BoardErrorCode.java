package memme.memoryme.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import memme.memoryme.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BoardErrorCode implements BaseErrorCode {
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "보드를 찾을 수 없습니다."),
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_002", "노트를 찾을 수 없습니다."),
    INVALID_BOARD_REQUEST(HttpStatus.BAD_REQUEST, "BOARD_003", "유효하지 않은 보드 요청입니다."),
    INVALID_NOTE_REQUEST(HttpStatus.BAD_REQUEST, "BOARD_004", "유효하지 않은 노트 요청입니다."),
    USER_UID_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_005", "해당 사용자 UID를 찾을 수 없습니다."),
    SAME_BOARD_MOVE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BOARD_006", "같은 보드로는 노트를 이동할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
