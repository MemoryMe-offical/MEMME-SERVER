package memme.memoryme.board.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.board.api.dto.*;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.CreateNoteRequest;
import memme.memoryme.note.api.dto.MoveNoteRequest;
import memme.memoryme.note.api.dto.NoteDto;
import memme.memoryme.note.api.dto.UpdateNoteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Board API", description = "보드 및 보드 하위 노트 API")
@RequestMapping("/v1/boards")
public interface BoardApi {
    @Operation(summary = "보드 생성")
    @PostMapping
    ResponseEntity<ResponseWrapper<BoardDto>> createBoard(@RequestBody CreateBoardRequest request);

    @Operation(summary = "보드 단건 조회")
    @GetMapping("/{boardUid}")
    ResponseEntity<ResponseWrapper<BoardDto>> getBoard(@PathVariable UUID boardUid);

    @Operation(summary = "보드 메타 수정")
    @PutMapping("/{boardUid}")
    ResponseEntity<ResponseWrapper<BoardDto>> updateBoard(
            @PathVariable UUID boardUid,
            @RequestBody UpdateBoardRequest request
    );

    @Operation(summary = "보드 삭제")
    @DeleteMapping("/{boardUid}")
    ResponseEntity<ResponseWrapper<Void>> deleteBoard(@PathVariable UUID boardUid);

    @Operation(summary = "보드 북마크 변경")
    @PatchMapping("/{boardUid}/bookmark")
    ResponseEntity<ResponseWrapper<BoardDto>> updateBookmark(
            @PathVariable UUID boardUid,
            @RequestBody(required = false) BoardBookmarkRequest request
    );

    @Operation(summary = "노트 생성")
    @PostMapping("/{boardUid}/notes")
    ResponseEntity<ResponseWrapper<NoteDto>> createNote(
            @Parameter(description = "보드 UID") @PathVariable UUID boardUid,
            @RequestBody CreateNoteRequest request
    );

    @Operation(summary = "노트 수정")
    @PutMapping("/{boardUid}/notes/{noteUid}")
    ResponseEntity<ResponseWrapper<NoteDto>> updateNote(
            @PathVariable UUID boardUid,
            @PathVariable UUID noteUid,
            @RequestBody UpdateNoteRequest request
    );

    @Operation(summary = "노트 삭제")
    @DeleteMapping("/{boardUid}/notes/{noteUid}")
    ResponseEntity<ResponseWrapper<Void>> deleteNote(
            @PathVariable UUID boardUid,
            @PathVariable UUID noteUid
    );

    @Operation(summary = "노트 다른 보드로 이동")
    @PatchMapping("/{boardUid}/notes/{noteUid}/move")
    ResponseEntity<ResponseWrapper<MoveNoteResponse>> moveNote(
            @PathVariable UUID boardUid,
            @PathVariable UUID noteUid,
            @RequestBody MoveNoteRequest request
    );
}
