package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "노트 이동 응답 DTO")
public record MoveNoteResponse(
        @Schema(description = "노트가 제거된 원본 보드")
        BoardDto sourceBoard,
        @Schema(description = "노트가 추가된 대상 보드")
        BoardDto targetBoard
) {
}
