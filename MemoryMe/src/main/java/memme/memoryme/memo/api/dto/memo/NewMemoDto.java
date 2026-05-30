package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.api.dto.FileAttachmentDto;

import java.util.List;

@Schema(description = "메모 생성 요청 DTO")
public record NewMemoDto(
        @Schema(description = "메모 내용 (레거시 호환 필드, 신규 요청은 content 사용)", example = "코딩 공부하기", deprecated = true, hidden = true)
        String text,
        @Schema(description = "메모 내용 (레거시 호환 필드, 신규 요청은 content 사용)", example = "코딩 공부하기", deprecated = true, hidden = true)
        String title,
        @Schema(description = "메모 내용. 첨부만 보내는 메모면 null 또는 생략 가능합니다.", example = "오늘 참고할 이미지")
        String content,
        @Schema(description = "첨부 이미지 URL 목록. 업로드 응답의 key를 imageKeys로 보내는 방식을 권장합니다.", example = "[\"/v1/upload/object?key=prod/memme/users/user-uid/images/image.webp\"]")
        List<String> imageUris,
        @Schema(description = "첨부 이미지 S3 key 목록. 이미지 하나만 보내는 메모는 content 없이 이 필드만 보내도 됩니다.", example = "[\"prod/memme/users/user-uid/images/image.webp\"]")
        List<String> imageKeys,
        @Schema(description = "첨부 영상 URL 목록. 업로드 응답의 key를 videoKeys로 보내는 방식을 권장합니다.", example = "[\"/v1/upload/object?key=prod/memme/users/user-uid/videos/video.mp4\"]")
        List<String> videoUris,
        @Schema(description = "첨부 영상 S3 key 목록. 영상 하나만 보내는 메모는 content 없이 이 필드만 보내도 됩니다.", example = "[\"prod/memme/users/user-uid/videos/video.mp4\"]")
        List<String> videoKeys,
        @Schema(description = "첨부 파일 목록. 파일 하나만 보내는 메모는 content 없이 이 필드만 보내도 됩니다.")
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
