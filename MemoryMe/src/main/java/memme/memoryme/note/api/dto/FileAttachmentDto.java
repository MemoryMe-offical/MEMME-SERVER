package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.NoteAttachment;

import java.util.UUID;
import java.util.function.Function;

@Schema(description = "첨부 파일 DTO")
public record FileAttachmentDto(
        @Schema(description = "파일 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "원본 파일명", example = "기획서.pdf")
        String name,
        @Schema(description = "파일 접근 URL", example = "/v1/upload/object?key=memme/users/.../file.pdf")
        String url,
        @Schema(description = "S3 객체 key")
        String key,
        @Schema(description = "MIME 타입", example = "application/pdf")
        String mimeType,
        @Schema(description = "파일 크기(bytes)", example = "204800")
        Long size,
        @Schema(description = "영상 썸네일 URL")
        String thumbnailUrl,
        @Schema(description = "영상 길이(초)", example = "42")
        Integer duration
) {
    public static FileAttachmentDto from(NoteAttachment attachment) {
        return from(attachment, null);
    }

    public static FileAttachmentDto from(NoteAttachment attachment, Function<NoteAttachment, String> urlResolver) {
        return new FileAttachmentDto(
                attachment.getUid(),
                attachment.getOriginalName(),
                urlResolver == null ? attachment.getUrl() : urlResolver.apply(attachment),
                attachment.getS3Key(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getThumbnailUrl(),
                attachment.getDurationSeconds()
        );
    }

    public NoteAttachment toEntity(AttachmentType type) {
        return toEntity(type, null);
    }

    public NoteAttachment toEntity(AttachmentType type, UUID userUid) {
        return NoteAttachment.builder()
                .uid(uid != null ? uid : UUID.randomUUID())
                .userUid(userUid)
                .type(type)
                .originalName(name)
                .url(url)
                .s3Key(key)
                .mimeType(mimeType)
                .sizeBytes(size)
                .thumbnailUrl(thumbnailUrl)
                .durationSeconds(duration)
                .build();
    }
}
