package memme.memoryme.note.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.controller.api.NoteApi;
import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;
import memme.memoryme.note.application.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NoteController implements NoteApi {
    private final NoteService noteService;

    @Override
    public ResponseEntity<ResponseWrapper<NoteDto>> toNoteDto(NewNoteDto newNote) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(
                    201,
                    "생성 성공",
                    noteService.createNote(newNote)
                )
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<NoteDto>> updateNote(NoteDto noteDto) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseWrapper.ok(
                        201,
                        "수정 성공",
                        noteService.updateNote(noteDto)
                )
        );
    }
}
