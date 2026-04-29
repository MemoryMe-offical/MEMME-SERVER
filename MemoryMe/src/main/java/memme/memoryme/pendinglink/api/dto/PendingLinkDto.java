package memme.memoryme.pendinglink.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.pendinglink.domain.PendingLink;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "공유 링크 임시 항목 DTO")
public record PendingLinkDto(
        @Schema(description = "PendingLink UID")
        UUID uid,
        @Schema(description = "원본 URL")
        String url,
        @Schema(description = "OG 미리보기 데이터")
        OgDataDto ogData,
        @Schema(description = "수신 시각")
        LocalDateTime receivedAt
) {
    public static PendingLinkDto from(PendingLink pendingLink) {
        return new PendingLinkDto(
                pendingLink.getUid(),
                pendingLink.getUrl(),
                OgDataDto.from(pendingLink),
                pendingLink.getReceivedAt()
        );
    }
}
