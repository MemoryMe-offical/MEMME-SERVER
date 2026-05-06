package memme.memoryme.memo.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.memo.api.dto.memo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Memo API", description = "빠른 메모 API")
@RequestMapping({"/v1/memos", "/v1/memo"})
public interface MemoApi {
    @Operation(summary = "메모 생성")
    @PostMapping
    ResponseEntity<ResponseWrapper<MemoDto>> createMemo(
            @Parameter(description = "새 메모")
            @RequestBody NewMemoDto request
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
