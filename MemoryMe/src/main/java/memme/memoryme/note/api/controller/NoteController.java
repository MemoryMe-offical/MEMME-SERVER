package memme.memoryme.note.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.controller.api.NoteApi;
import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;
import memme.memoryme.note.application.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NoteController implements NoteApi {
    private final NoteService noteService;

    @Override
    public ResponseEntity<ResponseWrapper<NoteDto>> toNoteDto(NewNoteDto newNote) {
        return null;
    }
}
