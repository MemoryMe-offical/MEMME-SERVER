package memme.memoryme.board.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.api.controller.api.BoardApi;
import memme.memoryme.board.api.dto.*;
import memme.memoryme.board.application.service.BoardService;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.CreateNoteRequest;
import memme.memoryme.note.api.dto.MoveNoteRequest;
import memme.memoryme.note.api.dto.NoteDto;
import memme.memoryme.note.api.dto.UpdateNoteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BoardController implements BoardApi {
    private final BoardService boardService;

    @Override
    public ResponseEntity<ResponseWrapper<BoardDto>> createBoard(CreateBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "보드 생성 성공", boardService.createBoard(request))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<BoardDto>> getBoard(UUID boardUid) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "보드 조회 성공", boardService.getBoard(boardUid)));
    }

    @Override
    public ResponseEntity<ResponseWrapper<BoardDto>> updateBoard(UUID boardUid, UpdateBoardRequest request) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "보드 수정 성공", boardService.updateBoard(boardUid, request)));
    }

    @Override
    public ResponseEntity<ResponseWrapper<Void>> deleteBoard(UUID boardUid) {
        boardService.deleteBoard(boardUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ResponseWrapper.ok(204, "보드 삭제 성공", null));
    }

    @Override
    public ResponseEntity<ResponseWrapper<BoardDto>> updateBookmark(UUID boardUid, BoardBookmarkRequest request) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "보드 북마크 변경 성공", boardService.updateBookmark(boardUid, request)));
    }

    @Override
    public ResponseEntity<ResponseWrapper<NoteDto>> createNote(UUID boardUid, CreateNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "노트 생성 성공", boardService.createNote(boardUid, request))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<NoteDto>> updateNote(UUID boardUid, UUID noteUid, UpdateNoteRequest request) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "노트 수정 성공", boardService.updateNote(boardUid, noteUid, request)));
    }

    @Override
    public ResponseEntity<ResponseWrapper<Void>> deleteNote(UUID boardUid, UUID noteUid) {
        boardService.deleteNote(boardUid, noteUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ResponseWrapper.ok(204, "노트 삭제 성공", null));
    }

    @Override
    public ResponseEntity<ResponseWrapper<MoveNoteResponse>> moveNotes(UUID boardUid, MoveNoteRequest request) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "노트 이동 성공", boardService.moveNotes(boardUid, request)));
    }

    @Override
    public ResponseEntity<ResponseWrapper<MoveNoteResponse>> moveNote(UUID boardUid, UUID noteUid, MoveNoteRequest request) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "노트 이동 성공", boardService.moveNote(boardUid, noteUid, request)));
    }
}
