package memme.memoryme.note.api.dto.note;

import memme.memoryme.note.api.dto.post.PostDto;
import memme.memoryme.note.domain.Note;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteDto(
        UUID uid,
        String title,
        PostDto post,
        LocalDateTime created,
        LocalDateTime updated
) {
    public static NoteDto from(Note note) {
        return new NoteDto(
                note.getUid(),
                note.getTitle(),
                note.getPost() != null ? PostDto.from(note.getPost()) : null,
                note.getCreated(),
                note.getUpdated()
        );
    }
}
