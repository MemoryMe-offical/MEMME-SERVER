package memme.memoryme.pendinglink.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "공유 링크 임시 목록 응답 DTO")
public record PendingLinkListResponse(
        @Schema(description = "대기 링크 목록")
        List<PendingLinkDto> pendingLinks,
        @Schema(description = "대기 링크 개수", example = "3")
        long count
) {
}
