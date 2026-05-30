package memme.memoryme.memo.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.memo.api.dto.memo.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Memo API", description = "빠른 메모 API")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 메모 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "메모 또는 보드를 찾을 수 없음")
})
@RequestMapping({"/v1/memos", "/v1/memo"})
public interface MemoApi {
    @Operation(summary = "메모 생성")
    @PostMapping
    ResponseEntity<ResponseWrapper<MemoDto>> createMemo(
            @Parameter(description = "새 메모")
            @RequestBody NewMemoDto request
    );

    @Operation(summary = "이미지 메모 생성", description = "이미지 파일 하나를 업로드하고 첨부 메모를 한 번에 생성합니다.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ResponseWrapper<MemoDto>> createImageMemo(
            @RequestPart(value = "content", required = false) String content,
            @RequestPart("file") MultipartFile file
    );

    @Operation(summary = "영상 메모 생성", description = "영상 파일 하나를 업로드하고 첨부 메모를 한 번에 생성합니다.")
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ResponseWrapper<MemoDto>> createVideoMemo(
            @RequestPart(value = "content", required = false) String content,
            @RequestPart("file") MultipartFile file
    );

    @Operation(summary = "파일 메모 생성", description = "파일 하나를 업로드하고 첨부 메모를 한 번에 생성합니다.")
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ResponseWrapper<MemoDto>> createFileMemo(
            @RequestPart(value = "content", required = false) String content,
            @RequestPart("file") MultipartFile file
    );

    @Operation(summary = "메모 삭제")
    @DeleteMapping("/{memoUid}")
    ResponseEntity<ResponseWrapper<Void>> deleteMemo(
            @Parameter(description = "메모 UID")
            @PathVariable UUID memoUid
    );

    @Operation(summary = "메모 북마크 변경")
    @PatchMapping("/{memoUid}/bookmark")
    ResponseEntity<ResponseWrapper<MemoDto>> updateBookmark(
            @Parameter(description = "메모 UID")
            @PathVariable UUID memoUid,
            @RequestBody(required = false) BookmarkRequest request
    );

    @Operation(summary = "메모를 새 보드로 변환")
    @PostMapping("/{memoUid}/convert/new-board")
    ResponseEntity<ResponseWrapper<BoardDto>> convertToNewBoard(
            @PathVariable UUID memoUid,
            @RequestBody ConvertMemoToNewBoardRequest request
    );

    @Operation(summary = "메모를 기존 보드의 노트로 변환")
    @PostMapping("/{memoUid}/convert/boards/{boardUid}")
    ResponseEntity<ResponseWrapper<BoardDto>> convertToExistingBoard(
            @PathVariable UUID memoUid,
            @PathVariable UUID boardUid,
            @RequestBody(required = false) ConvertMemoToExistingBoardRequest request
    );
}
