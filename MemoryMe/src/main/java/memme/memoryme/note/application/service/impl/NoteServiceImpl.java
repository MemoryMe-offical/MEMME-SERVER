package memme.memoryme.note.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;
import memme.memoryme.note.application.service.NoteService;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.infra.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;

    @Override
    @Transactional
    public NoteDto createNote(NewNoteDto newNoteDto) {
        Note note = noteRepository.save(
                Note.builder()
                        .uid(UUID.randomUUID())
                        .title(newNoteDto.title())
                        .build()
        );

        return toNoteDto(note);
    }

    // Page || Slice 고민중
    public List<NoteDto> getUserNotes() {
        return List.of();
    }

    private NoteDto toNoteDto(Note note) {
        return NoteDto.from(note);
    }
}
