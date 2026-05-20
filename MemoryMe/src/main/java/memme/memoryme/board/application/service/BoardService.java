package memme.memoryme.board.application.service;

import memme.memoryme.board.api.dto.*;
import memme.memoryme.note.api.dto.CreateNoteRequest;
import memme.memoryme.note.api.dto.MoveNoteRequest;
import memme.memoryme.note.api.dto.NoteDto;
import memme.memoryme.note.api.dto.UpdateNoteRequest;

import java.util.UUID;

public interface BoardService {
    BoardDto createBoard(CreateBoardRequest request);
    BoardDto getBoard(UUID boardUid);
    BoardDto updateBoard(UUID boardUid, UpdateBoardRequest request);
    void deleteBoard(UUID boardUid);
    BoardDto updateBookmark(UUID boardUid, BoardBookmarkRequest request);
    NoteDto createNote(UUID boardUid, CreateNoteRequest request);
    NoteDto updateNote(UUID boardUid, UUID noteUid, UpdateNoteRequest request);
    void deleteNote(UUID boardUid, UUID noteUid);
    MoveNoteResponse moveNotes(UUID sourceBoardUid, MoveNoteRequest request);
    MoveNoteResponse moveNote(UUID sourceBoardUid, UUID noteUid, MoveNoteRequest request);
}
