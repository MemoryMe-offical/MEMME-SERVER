package memme.memoryme.note.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.note.api.controller.api.NoteApi;
import memme.memoryme.note.application.service.NoteService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NoteController implements NoteApi {
    private final NoteService noteService;
}
