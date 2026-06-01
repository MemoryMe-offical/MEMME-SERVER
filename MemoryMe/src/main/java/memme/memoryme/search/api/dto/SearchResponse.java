package memme.memoryme.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "통합 검색 응답")
public record SearchResponse(
        @Schema(description = "검색 결과 목록")
        List<SearchItemDto> items,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "다음 페이지 커서")
        String nextCursor,

        @Schema(description = "요청에 적용된 조회 개수", example = "20")
        int limit,

        @Schema(description = "검색 조건에 매칭된 전체 문서 수", example = "127")
        long totalCount
) {
}
