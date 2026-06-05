package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.domain.NoteAttachment;

import java.util.UUID;
import java.util.function.Function;

@Schema(description = "이미지·영상 첨부 DTO")
public record MediaAttachmentDto(
        @Schema(description = "첨부 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "원본 파일명", example = "photo.jpg")
        String name,
        @Schema(description = "접근 URL", example = "https://s3-presigned-url...")
        String url,
        @Schema(description = "S3 객체 key", example = "prod/memme/users/user-uid/images/image.webp")
        String key,
        @Schema(description = "MIME 타입", example = "image/webp")
        String mimeType,
        @Schema(description = "파일 크기(bytes)", example = "204800")
        Long size,
        @Schema(description = "영상 썸네일 URL", example = "https://example.com/thumb.jpg")
        String thumbnailUrl,
        @Schema(description = "영상 길이(초)", example = "42")
        Integer duration
) {
    public static MediaAttachmentDto from(NoteAttachment attachment, Function<NoteAttachment, String> urlResolver) {
        return new MediaAttachmentDto(
                attachment.getUid(),
                attachment.getOriginalName(),
                urlResolver == null ? attachment.getUrl() : urlResolver.apply(attachment),
                resolveKey(attachment),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getThumbnailUrl(),
                attachment.getDurationSeconds()
        );
    }

    private static String resolveKey(NoteAttachment attachment) {
        if (attachment.getS3Key() != null && !attachment.getS3Key().isBlank()) {
            return attachment.getS3Key();
        }
        String url = attachment.getUrl();
        if (url == null || url.isBlank()) {
            return null;
        }
        String normalized = url.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("/")) {
            return null;
        }
        return normalized.contains("/users/")
                && (normalized.contains("/images/") || normalized.contains("/videos/") || normalized.contains("/files/"))
                ? normalized
                : null;
    }
}
