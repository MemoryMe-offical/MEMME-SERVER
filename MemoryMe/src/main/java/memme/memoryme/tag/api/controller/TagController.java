package memme.memoryme.tag.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.tag.api.dto.TagListResponse;
import memme.memoryme.tag.application.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tag API", description = "보드 태그 조회 API")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "태그 조회 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 태그 조회 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
})
@RestController
@RequestMapping("/v1/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @Operation(summary = "태그 목록 조회")
    @GetMapping
    public ResponseEntity<ResponseWrapper<TagListResponse>> getTags(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "태그 조회 성공", tagService.getTags(q)));
    }
}
