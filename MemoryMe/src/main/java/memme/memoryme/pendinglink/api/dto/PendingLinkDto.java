package memme.memoryme.pendinglink.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.pendinglink.domain.PendingLink;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "공유 링크 임시 항목 DTO")
public record PendingLinkDto(
        @Schema(description = "PendingLink UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "원본 URL", example = "https://example.com/article")
        String url,
        @Schema(description = "OG 미리보기 데이터")
        OgDataDto ogData,
        @Schema(description = "수신 시각", example = "2026-05-20T14:30:00")
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
