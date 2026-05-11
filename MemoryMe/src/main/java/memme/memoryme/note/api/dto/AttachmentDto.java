package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.board.domain.Board;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Schema(description = "첨부파일 응답 DTO")
public record AttachmentDto(
        @Schema(description = "첨부파일 UID")
        UUID uid,
        @Schema(description = "첨부파일 타입", example = "IMAGE")
        AttachmentType type,
        @Schema(description = "원본 파일명")
        String name,
        @Schema(description = "객체 접근 URL")
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
        Integer duration,
        @Schema(description = "연결된 노트 UID")
        UUID noteUid,
        @Schema(description = "연결된 보드 UID")
        UUID boardUid,
        @Schema(description = "생성 시각")
        LocalDateTime createdAt
) {
    public static AttachmentDto from(NoteAttachment attachment) {
        return from(attachment, null);
    }

    public static AttachmentDto from(NoteAttachment attachment, Function<NoteAttachment, String> urlResolver) {
        Note note = attachment.getNote();
        Board board = note == null ? null : note.getBoard();
        return new AttachmentDto(
                attachment.getUid(),
                attachment.getType(),
                attachment.getOriginalName(),
                urlResolver == null ? attachment.getUrl() : urlResolver.apply(attachment),
                resolveKey(attachment),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getThumbnailUrl(),
                attachment.getDurationSeconds(),
                note == null ? null : note.getUid(),
                board == null ? null : board.getUid(),
                attachment.getCreatedAt()
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
