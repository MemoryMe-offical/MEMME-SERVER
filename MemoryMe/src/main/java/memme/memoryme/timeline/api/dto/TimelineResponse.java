package memme.memoryme.timeline.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "타임라인 목록 응답 DTO")
public record TimelineResponse(
        @Schema(description = "메모와 보드가 섞인 타임라인 항목")
        List<Object> items,
        @Schema(description = "다음 slice 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "다음 slice 조회용 cursor", example = "MjAyNi0wNS0yMFQxNDozMDowMHw1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDA")
        String nextCursor,
        @Schema(description = "요청 slice 크기", example = "50")
        int limit
) {
}
