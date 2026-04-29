package memme.memoryme.og.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.OgDataDto;
import memme.memoryme.og.application.service.OgService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OG API", description = "URL OG 메타데이터 조회 API")
@RestController
@RequestMapping("/v1/og")
@RequiredArgsConstructor
public class OgController {
    private final OgService ogService;

    @Operation(summary = "OG 메타데이터 조회")
    @GetMapping
    public ResponseEntity<ResponseWrapper<OgDataDto>> getOg(@RequestParam String url) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "OG 데이터 조회 성공", ogService.fetch(url)));
    }
}
