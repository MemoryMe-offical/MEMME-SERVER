package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.board.domain.Board;
import memme.memoryme.note.api.dto.NoteDto;
import memme.memoryme.note.domain.NoteAttachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Schema(description = "보드 응답 DTO")
public record BoardDto(
        @Schema(description = "보드 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "타임라인 타입", example = "board")
        String type,
        @Schema(description = "보드 제목", example = "여름 MT")
        String title,
        @Schema(description = "보드 설명", example = "MT 준비 자료와 공지 모음")
        String description,
        @Schema(description = "태그 목록", example = "[\"mt\", \"공지\"]")
        List<String> tags,
        @Schema(description = "노트 목록")
        List<NoteDto> notes,
        @Schema(description = "북마크 여부", example = "true")
        boolean bookmarked,
        @Schema(description = "생성 시각", example = "2026-05-20T14:30:00")
        LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-05-20T15:10:00")
        LocalDateTime updatedAt
) {
    public static BoardDto from(Board board) {
        return from(board, null);
    }

    public static BoardDto from(Board board, Function<NoteAttachment, String> attachmentUrlResolver) {
        return new BoardDto(
                board.getUid(),
                "board",
                board.getTitle(),
                board.getDescription(),
                List.copyOf(board.getTags()),
                board.getNotes().stream().map(note -> NoteDto.from(note, attachmentUrlResolver)).toList(),
                board.isBookmarked(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
