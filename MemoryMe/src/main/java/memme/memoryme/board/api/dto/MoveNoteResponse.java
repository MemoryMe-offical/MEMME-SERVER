package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "노트 이동 응답 DTO")
public record MoveNoteResponse(
        @Schema(description = "이동된 노트 UID 목록", example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"660e8400-e29b-41d4-a716-446655440000\"]")
        List<UUID> movedNoteUids,
        @Schema(description = "이동된 노트 개수", example = "2")
        int movedCount,
        @Schema(description = "노트가 제거된 원본 보드")
        BoardDto sourceBoard,
        @Schema(description = "노트가 추가된 대상 보드")
        BoardDto targetBoard
) {
}
