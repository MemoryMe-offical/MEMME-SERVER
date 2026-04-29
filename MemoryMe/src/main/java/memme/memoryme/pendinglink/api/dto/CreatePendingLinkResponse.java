package memme.memoryme.pendinglink.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공유 링크 임시 저장 응답 DTO")
public record CreatePendingLinkResponse(
        @Schema(description = "저장된 PendingLink")
        PendingLinkDto pendingLink
) {
}
