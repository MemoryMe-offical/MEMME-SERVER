package memme.memoryme.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "검색 재색인 응답")
public record SearchReindexResponse(
        @Schema(description = "검색 인덱스 이름", example = "memme_search")
        String indexName,

        @Schema(description = "재색인 대상 사용자 UID", example = "025858c9-7623-4c14-a6b8-6be6020966e4")
        UUID userUid,

        @Schema(description = "색인된 전체 문서 수", example = "17")
        int indexedCount,

        @Schema(description = "색인된 메모 수", example = "5")
        int memoCount,

        @Schema(description = "색인된 보드 수", example = "3")
        int boardCount,

        @Schema(description = "색인된 노트 수", example = "9")
        int noteCount
) {
}
