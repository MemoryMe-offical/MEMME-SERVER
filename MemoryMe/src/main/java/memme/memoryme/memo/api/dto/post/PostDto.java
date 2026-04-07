package memme.memoryme.memo.api.dto.post;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.memo.domain.Post;

import java.util.List;

@Schema(description = "메모 본문 DTO")
public record PostDto(

        @Schema(
                description = "메모 본문 내용",
                example = "오늘은 회의 내용을 정리하고 다음 작업 일정을 정했다."
        )
        String content,

        @ArraySchema(
                schema = @Schema(
                        description = "본문에 포함된 이미지 URL (추후 변경될 수 있음)",
                        example = "https://cdn.example.com/images/note-1.png"
                )
        )
        List<String> images,

        @ArraySchema(
                schema = @Schema(
                        description = "본문에 첨부된 파일 URL (추후 변경될 수 있음)",
                        example = "https://cdn.example.com/files/meeting-notes.pdf"
                )
        )
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
