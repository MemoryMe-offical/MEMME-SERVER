package memme.memoryme.note.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.token.CurrentUserProvider;
import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;
import memme.memoryme.note.api.dto.post.PostDto;
import memme.memoryme.note.application.service.NoteService;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.Post;
import memme.memoryme.note.infra.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public NoteDto createNote(NewNoteDto newNoteDto) {

        Note note = noteRepository.save(
                Note.builder()
                        .uid(UUID.randomUUID())
                        // todo: userId의 타입 확인 후 변경
                        .userId(Long.parseLong(currentUserProvider.getUserId()))
                        .title(newNoteDto.title())
                        .build()
        );

        return toNoteDto(note);
    }

    @Override
    @Transactional
    public NoteDto updateNote(NoteDto noteDto) {
        Note note = noteRepository.findByUid(noteDto.uid())
                // todo: 예외 처리 핸들러 작성 필요
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (noteDto.post() == null) {
            note.setPost(null);
        } else {
            Post post = note.getPost();
            if (post == null) {
                post = new Post();
                note.setPost(post);
            }
            updatePost(post, noteDto.post());
        }

        return toNoteDto(note);
    }

    // Page || Slice 고민중
    public List<NoteDto> getUserNotes() {
        return List.of();
    }

    private NoteDto toNoteDto(Note note) {
        return NoteDto.from(note);
    }

    private static void updatePost(Post post, PostDto postDto) {
        post.setContent(postDto.content());
        post.setImages(postDto.images());
        post.setFiles(postDto.files());
    }
}
