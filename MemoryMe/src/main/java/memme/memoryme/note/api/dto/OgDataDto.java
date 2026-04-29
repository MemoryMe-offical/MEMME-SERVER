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
        String siteName
) {
    public static OgDataDto from(Note note) {
        if (note.getOgTitle() == null
                && note.getOgDescription() == null
                && note.getOgImageUrl() == null
                && note.getOgSiteName() == null) {
            return null;
        }
        return new OgDataDto(
                note.getOgTitle(),
                note.getOgDescription(),
                note.getOgImageUrl(),
                note.getOgSiteName()
        );
    }

    public static OgDataDto from(PendingLink pendingLink) {
        if (pendingLink.getOgTitle() == null
                && pendingLink.getOgDescription() == null
                && pendingLink.getOgImageUrl() == null
                && pendingLink.getOgSiteName() == null) {
            return null;
        }
        return new OgDataDto(
                pendingLink.getOgTitle(),
                pendingLink.getOgDescription(),
                pendingLink.getOgImageUrl(),
                pendingLink.getOgSiteName()
        );
    }
}
