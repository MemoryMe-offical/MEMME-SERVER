package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보드 북마크 변경 요청 DTO")
public record BoardBookmarkRequest(
        @Schema(description = "북마크 여부", example = "true")
        Boolean bookmarked,
        @Schema(description = "기존 프론트 호환용 북마크 여부", example = "true")
        Boolean bookMark
) {
    public boolean valueOrFalse() {
        return Boolean.TRUE.equals(bookmarked) || Boolean.TRUE.equals(bookMark);
    }
}
