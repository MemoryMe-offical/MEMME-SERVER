package memme.memoryme.note.api.dto.note;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.api.dto.post.PostDto;
import memme.memoryme.note.domain.Note;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "메모 응답 DTO")
public record NoteDto(

        @Schema(
                description = "메모 고유 식별자",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID uid,

        @Schema(
                description = "메모 제목",
                example = "오늘 회의 정리"
        )
        String title,

        @Schema(
                description = "메모 본문"
        )
        PostDto post,

        @Schema(
                description = "메모 생성 시각",
                example = "2026-03-20T14:30:00"
        )
        LocalDateTime created,

        @Schema(
                description = "메모 수정 시각",
                example = "2026-03-20T15:10:00"
        )
        LocalDateTime updated
) {
    public static NoteDto from(Note note) {
        return new NoteDto(
                note.getUid(),
                note.getTitle(),
                note.getPost() != null ? PostDto.from(note.getPost()) : null,
                note.getCreated(),
                note.getUpdated()
        );
    }
}
