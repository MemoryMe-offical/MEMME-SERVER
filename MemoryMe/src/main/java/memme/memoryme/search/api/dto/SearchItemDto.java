package memme.memoryme.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "통합 검색 결과 항목")
public record SearchItemDto(
        @Schema(description = "결과 타입", example = "note", allowableValues = {"memo", "board", "note"})
        String type,

        @Schema(description = "결과 UID", example = "c11287ff-0862-41c2-8ae4-ae378900173c")
        String uid,

        @Schema(description = "표시 제목", example = "hello")
        String title,

        @Schema(description = "본문 또는 설명", example = "테스트입니다")
        String content,

        @Schema(description = "목록 표시용 요약 문구", example = "테스트입니다")
        String snippet,

        @Schema(description = "부모 타입. 노트는 board, 메모/보드는 null", example = "board")
        String parentType,

        @Schema(description = "부모 UID. 노트 검색 결과에서 원래 보드로 이동할 때 사용", example = "ae46ed58-43ff-40c6-941c-46d9efe3cc8d")
        String parentUid,

        @Schema(description = "부모 제목", example = "hi")
        String parentTitle,

        @Schema(description = "태그 목록", example = "[\"work\", \"link\"]")
        List<String> tags,

        @Schema(description = "링크 목록", example = "[\"https://naver.com\"]")
        List<String> urls,

        @Schema(description = "첨부 파일명 목록", example = "[\"sample.jpg\"]")
        List<String> attachmentNames,

        @Schema(description = "북마크 여부", example = "false")
        boolean bookmarked,

        @Schema(description = "생성 시각", example = "2026-05-11T14:37:03.508542")
        String createdAt,

        @Schema(description = "수정 시각", example = "2026-05-12T03:04:59.272733")
        String updatedAt,

        @Schema(description = "검색 점수", example = "12.345")
        Double score
) {
}
