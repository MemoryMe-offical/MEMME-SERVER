package memme.memoryme.pendinglink.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공유 링크 임시 저장 요청 DTO")
public record CreatePendingLinkRequest(
        @Schema(description = "공유된 원본 URL", example = "https://example.com/article")
        String url
) {
}
