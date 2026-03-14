package memme.memoryme.note.api.dto;

import memme.memoryme.note.domain.NotePost;

import java.util.List;

public record PostDto(
        String content,
        List<String> images,
        List<String> files
) {
    public static PostDto from(NotePost notePost) {
        return new PostDto(
                notePost.getContent(),
                notePost.getImages(),
                notePost.getFiles()
        );
    }
}
