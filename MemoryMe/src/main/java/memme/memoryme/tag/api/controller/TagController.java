package memme.memoryme.tag.api.controller;

import io.swagger.v3.oas.annotations.Operation;
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
