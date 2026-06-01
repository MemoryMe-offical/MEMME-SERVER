package memme.memoryme.search.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.search.api.dto.SearchReindexResponse;
import memme.memoryme.search.api.dto.SearchResponse;
import memme.memoryme.search.application.service.SearchQueryService;
import memme.memoryme.search.application.service.SearchReindexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search API", description = "통합 검색 API")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 검색 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "503", description = "검색 기능 비활성화 또는 검색 인프라 오류")
})
@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchQueryService searchQueryService;
    private final SearchReindexService searchReindexService;

    @Operation(
            summary = "통합 검색",
            description = "현재 로그인한 사용자의 메모, 보드, 노트를 Elasticsearch에서 통합 검색합니다. 다음 페이지는 응답의 nextCursor를 cursor로 다시 전달합니다."
    )
    @GetMapping
    public ResponseEntity<ResponseWrapper<SearchResponse>> search(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        return ResponseEntity.ok(ResponseWrapper.ok(
                200,
                "통합 검색 성공",
                searchQueryService.search(q, type, cursor, limit)
        ));
    }

    @Operation(
            summary = "현재 사용자 검색 색인 재생성",
            description = "현재 로그인한 사용자의 메모, 보드, 노트를 Elasticsearch 검색 인덱스에 다시 적재합니다."
    )
    @PostMapping("/reindex")
    public ResponseEntity<ResponseWrapper<SearchReindexResponse>> reindexCurrentUser() {
        return ResponseEntity.ok(ResponseWrapper.ok(
                200,
                "검색 색인 재생성 성공",
                searchReindexService.reindexCurrentUser()
        ));
    }
}
