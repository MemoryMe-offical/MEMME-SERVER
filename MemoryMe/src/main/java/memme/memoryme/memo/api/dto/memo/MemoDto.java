package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.memo.domain.Memo;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "메모 응답 DTO")
public record MemoDto(
        @Schema(description = "메모 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,

        @Schema(description = "타임라인 타입", example = "memo")
        String type,

        @Schema(description = "메모 내용", example = "코딩 공부하기")
        String text,

        @Schema(description = "북마크 여부", example = "false")
        boolean bookmarked,

        @Schema(description = "생성 시각", example = "2026-04-11T10:00:00")
        LocalDateTime createdAt
) {
    public static MemoDto from(Memo memo) {
        return new MemoDto(
                memo.getUid(),
                "memo",
                memo.getText(),
                memo.isBookmarked(),
                memo.getCreatedAt()
        );
    }
}
