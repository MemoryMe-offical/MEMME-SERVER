package memme.memoryme.note.api.dto.post;

import memme.memoryme.note.domain.Post;

import java.util.List;

public record PostDto(
        String content,
        List<String> images,
        List<String> files
) {
    public static PostDto from(Post post) {
        return new PostDto(
                post.getContent(),
                post.getImages(),
                post.getFiles()
        );
    }
}
