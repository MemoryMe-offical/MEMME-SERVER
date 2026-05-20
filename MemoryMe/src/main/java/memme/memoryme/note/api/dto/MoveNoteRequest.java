package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Schema(description = "노트 이동 요청 DTO")
public record MoveNoteRequest(
        @Schema(description = "이동할 노트 UID 목록")
        List<UUID> noteUids,
        @Schema(description = "이동할 노트 UID (단건 호환)")
        UUID noteUid,
        @Schema(description = "이동할 노트 UID (기존 프론트 호환)")
        UUID noteId,
        @Schema(description = "대상 보드 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID targetBoardUid,
        @Schema(description = "기존 프론트 호환용 대상 보드 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID targetBoardId,
        @Schema(description = "기존 프론트 호환용 대상 보드 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID boardUid,
        @Schema(description = "기존 프론트 호환용 대상 보드 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID boardId
) {
    public UUID resolvedTargetBoardUid() {
        if (targetBoardUid != null) {
            return targetBoardUid;
        }
        if (targetBoardId != null) {
            return targetBoardId;
        }
        if (boardUid != null) {
            return boardUid;
        }
        return boardId;
    }

    public List<UUID> resolvedNoteUids(UUID pathNoteUid) {
        LinkedHashSet<UUID> resolved = new LinkedHashSet<>();
        if (pathNoteUid != null) {
            resolved.add(pathNoteUid);
        }
        if (noteUids != null) {
            noteUids.stream()
                    .filter(uid -> uid != null)
                    .forEach(resolved::add);
        }
        if (noteUid != null) {
            resolved.add(noteUid);
        }
        if (noteId != null) {
            resolved.add(noteId);
        }
        return new ArrayList<>(resolved);
    }
}
