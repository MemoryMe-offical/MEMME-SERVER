package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "첨부파일 목록 응답 DTO")
public record AttachmentListResponse(
        @Schema(description = "첨부파일 목록")
        List<AttachmentDto> items,
        @Schema(description = "전체 개수", example = "42")
        long total,
        @Schema(description = "현재 페이지", example = "1")
        int page,
        @Schema(description = "페이지 크기", example = "50")
        int limit
) {
}
