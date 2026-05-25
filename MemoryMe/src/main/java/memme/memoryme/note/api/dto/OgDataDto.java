package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.domain.Note;
import memme.memoryme.pendinglink.domain.PendingLink;

@Schema(description = "OG 미리보기 데이터 DTO")
public record OgDataDto(
        @Schema(description = "OG title", example = "Article Title")
        String title,
        @Schema(description = "OG description", example = "Article description")
        String description,
        @Schema(description = "OG image URL", example = "https://example.com/og.jpg")
        String imageUrl,
        @Schema(description = "OG site name", example = "Example")
        String siteName,
        @Schema(description = "AI 요약", example = "이 링크는 서비스 출시 배경과 핵심 기능을 짧게 소개합니다.")
        String summary
) {
    public static OgDataDto from(Note note) {
        if (note.getOgTitle() == null
                && note.getOgDescription() == null
                && note.getOgImageUrl() == null
                && note.getOgSiteName() == null
                && note.getOgSummary() == null) {
            return null;
        }
        return new OgDataDto(
                note.getOgTitle(),
                note.getOgDescription(),
                note.getOgImageUrl(),
                note.getOgSiteName(),
                note.getOgSummary()
        );
    }

    public static OgDataDto from(PendingLink pendingLink) {
        if (pendingLink.getOgTitle() == null
                && pendingLink.getOgDescription() == null
                && pendingLink.getOgImageUrl() == null
                && pendingLink.getOgSiteName() == null
                && pendingLink.getOgSummary() == null) {
            return null;
        }
        return new OgDataDto(
                pendingLink.getOgTitle(),
                pendingLink.getOgDescription(),
                pendingLink.getOgImageUrl(),
                pendingLink.getOgSiteName(),
                pendingLink.getOgSummary()
        );
    }
}
