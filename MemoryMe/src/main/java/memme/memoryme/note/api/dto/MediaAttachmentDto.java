package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.domain.NoteAttachment;

import java.util.UUID;
import java.util.function.Function;

@Schema(description = "이미지·영상 첨부 DTO")
public record MediaAttachmentDto(
        @Schema(description = "첨부 UID")
        UUID uid,
        @Schema(description = "접근 URL")
        String url,
        @Schema(description = "S3 객체 key")
        String key,
        @Schema(description = "MIME 타입")
        String mimeType,
        @Schema(description = "파일 크기(bytes)")
        Long size,
        @Schema(description = "영상 썸네일 URL")
        String thumbnailUrl,
        @Schema(description = "영상 길이(초)")
        Integer duration
) {
    public static MediaAttachmentDto from(NoteAttachment attachment, Function<NoteAttachment, String> urlResolver) {
        return new MediaAttachmentDto(
                attachment.getUid(),
                urlResolver == null ? attachment.getUrl() : urlResolver.apply(attachment),
                attachment.getS3Key(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getThumbnailUrl(),
                attachment.getDurationSeconds()
        );
    }
}
