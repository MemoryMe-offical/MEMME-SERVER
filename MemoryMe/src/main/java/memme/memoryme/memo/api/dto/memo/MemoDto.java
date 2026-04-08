package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.memo.api.dto.post.PostDto;
import memme.memoryme.memo.domain.Memo;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "메모 응답 DTO")
public record MemoDto(

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
    public static MemoDto from(Memo memo) {
        return new MemoDto(
                memo.getUid(),
                memo.getTitle(),
                memo.getPost() != null ? PostDto.from(memo.getPost()) : null,
                memo.getCreated(),
                memo.getUpdated()
        );
    }
}
