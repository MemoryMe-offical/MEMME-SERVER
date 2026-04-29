package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.NoteAttachment;

import java.util.UUID;

@Schema(description = "첨부 파일 DTO")
public record FileAttachmentDto(
        @Schema(description = "파일 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "원본 파일명", example = "기획서.pdf")
        String name,
        @Schema(description = "파일 URL", example = "https://cdn.example.com/uploads/plan.pdf")
        String url,
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
        return new FileAttachmentDto(
                attachment.getUid(),
                attachment.getOriginalName(),
                attachment.getUrl(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getThumbnailUrl(),
                attachment.getDurationSeconds()
        );
    }

    public NoteAttachment toEntity(AttachmentType type) {
        return NoteAttachment.builder()
                .uid(uid != null ? uid : UUID.randomUUID())
                .type(type)
                .originalName(name)
                .url(url)
                .mimeType(mimeType)
                .sizeBytes(size)
                .thumbnailUrl(thumbnailUrl)
                .durationSeconds(duration)
                .build();
    }
}
