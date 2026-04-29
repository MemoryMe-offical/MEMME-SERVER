package memme.memoryme.timeline.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "타임라인 목록 응답 DTO")
public record TimelineResponse(
        @Schema(description = "메모와 보드가 섞인 타임라인 항목")
        List<Object> items,
        @Schema(description = "전체 개수", example = "120")
        long total,
        @Schema(description = "현재 페이지", example = "1")
        int page,
        @Schema(description = "페이지 크기", example = "50")
        int limit
) {
}
