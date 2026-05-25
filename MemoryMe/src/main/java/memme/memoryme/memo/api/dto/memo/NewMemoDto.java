package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.api.dto.FileAttachmentDto;

import java.util.List;

@Schema(description = "메모 생성 요청 DTO")
public record NewMemoDto(
        @Schema(description = "빠르게 남기는 메모 내용", example = "코딩 공부하기")
        String text,
        @Schema(description = "기존 프론트 호환용 메모 내용", example = "코딩 공부하기")
        String title,
        @Schema(description = "기존 프론트 호환용 메모 내용", example = "코딩 공부하기")
        String content,
        @Schema(description = "첨부 이미지 URL 목록", example = "[\"/v1/upload/object?key=prod/memme/users/user-uid/images/image.webp\"]")
        List<String> imageUris,
        @Schema(description = "첨부 이미지 S3 key 목록", example = "[\"prod/memme/users/user-uid/images/image.webp\"]")
        List<String> imageKeys,
        @Schema(description = "첨부 영상 URL 목록", example = "[\"/v1/upload/object?key=prod/memme/users/user-uid/videos/video.mp4\"]")
        List<String> videoUris,
        @Schema(description = "첨부 영상 S3 key 목록", example = "[\"prod/memme/users/user-uid/videos/video.mp4\"]")
        List<String> videoKeys,
        @Schema(description = "첨부 파일 목록")
        List<FileAttachmentDto> files
) {
    public String resolvedText() {
        if (text != null && !text.isBlank()) {
            return text;
        }
        if (title != null && !title.isBlank()) {
            return title;
        }
        return content;
    }
}
