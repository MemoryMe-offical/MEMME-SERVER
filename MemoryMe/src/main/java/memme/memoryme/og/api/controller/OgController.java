package memme.memoryme.og.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "OG 데이터 조회 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 OG 조회 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
})
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
