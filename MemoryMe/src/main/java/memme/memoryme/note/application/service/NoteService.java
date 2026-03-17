package memme.memoryme.note.application.service;

import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;

public interface NoteService {
    NoteDto createNote(NewNoteDto newNoteDto);
    NoteDto updateNote(NoteDto noteDto);
}
