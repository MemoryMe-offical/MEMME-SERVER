package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.board.domain.Board;
import memme.memoryme.note.api.dto.NoteDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "보드 응답 DTO")
public record BoardDto(
        @Schema(description = "보드 UID")
        UUID uid,
        @Schema(description = "타임라인 타입", example = "board")
        String type,
        @Schema(description = "보드 제목")
        String title,
        @Schema(description = "보드 설명")
        String description,
        @Schema(description = "태그 목록")
        List<String> tags,
        @Schema(description = "노트 목록")
        List<NoteDto> notes,
        @Schema(description = "북마크 여부")
        boolean bookmarked,
        @Schema(description = "생성 시각")
        LocalDateTime createdAt,
        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {
    public static BoardDto from(Board board) {
        return new BoardDto(
                board.getUid(),
                "board",
                board.getTitle(),
                board.getDescription(),
                List.copyOf(board.getTags()),
                board.getNotes().stream().map(NoteDto::from).toList(),
                board.isBookmarked(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
