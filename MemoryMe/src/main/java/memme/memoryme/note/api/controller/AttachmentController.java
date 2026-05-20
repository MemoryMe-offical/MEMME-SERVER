package memme.memoryme.note.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.AttachmentDto;
import memme.memoryme.note.api.dto.AttachmentListResponse;
import memme.memoryme.note.application.service.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Attachment API", description = "노트에 연결된 첨부파일 조회·삭제 API")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 첨부파일 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "첨부파일을 찾을 수 없음")
})
@RestController
@RequestMapping("/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @Operation(summary = "첨부파일 목록 조회")
    @GetMapping
    public ResponseEntity<ResponseWrapper<AttachmentListResponse>> getAttachments(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer limit
    ) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "첨부파일 목록 조회 성공", attachmentService.getAttachments(type, page, limit)));
    }

    @Operation(summary = "첨부파일 단건 조회")
    @GetMapping("/{attachmentUid}")
    public ResponseEntity<ResponseWrapper<AttachmentDto>> getAttachment(@PathVariable UUID attachmentUid) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "첨부파일 조회 성공", attachmentService.getAttachment(attachmentUid)));
    }

    @Operation(summary = "첨부파일 삭제")
    @DeleteMapping("/{attachmentUid}")
    public ResponseEntity<ResponseWrapper<Void>> deleteAttachment(@PathVariable UUID attachmentUid) {
        attachmentService.deleteAttachment(attachmentUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ResponseWrapper.ok(204, "첨부파일 삭제 성공", null));
    }
}
