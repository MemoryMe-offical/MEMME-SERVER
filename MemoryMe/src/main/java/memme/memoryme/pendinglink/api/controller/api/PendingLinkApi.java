package memme.memoryme.pendinglink.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkRequest;
import memme.memoryme.pendinglink.api.dto.CreatePendingLinkResponse;
import memme.memoryme.pendinglink.api.dto.PendingLinkListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Pending Link API", description = "외부 공유 링크 임시 보관 API")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 링크 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "대기 중인 링크를 찾을 수 없음")
})
@RequestMapping("/v1/pending-links")
public interface PendingLinkApi {
    @Operation(summary = "공유 링크 임시 저장")
    @PostMapping
    ResponseEntity<ResponseWrapper<CreatePendingLinkResponse>> create(@RequestBody CreatePendingLinkRequest request);

    @Operation(summary = "공유 링크 임시 목록 조회")
    @GetMapping
    ResponseEntity<ResponseWrapper<PendingLinkListResponse>> getPendingLinks();

    @Operation(summary = "공유 링크 임시 항목 삭제")
    @DeleteMapping("/{pendingLinkUid}")
    ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable UUID pendingLinkUid);
}
