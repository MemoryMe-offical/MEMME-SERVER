package memme.memoryme.note.api.dto.note;

public record NewNoteDto(
        Long userId,
        String title
) {
}
