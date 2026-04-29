package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "노트 이동 요청 DTO")
public record MoveNoteRequest(
        @Schema(description = "대상 보드 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID targetBoardUid
) {
}
