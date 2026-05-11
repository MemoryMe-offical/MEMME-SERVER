package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.board.domain.Board;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;

import java.time.LocalDateTime;
import java.util.UUID;

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
        Note note = attachment.getNote();
        Board board = note == null ? null : note.getBoard();
        return new AttachmentDto(
                attachment.getUid(),
                attachment.getType(),
                attachment.getOriginalName(),
                attachment.getUrl(),
                attachment.getS3Key(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getThumbnailUrl(),
                attachment.getDurationSeconds(),
                note == null ? null : note.getUid(),
                board == null ? null : board.getUid(),
                attachment.getCreatedAt()
        );
    }
}
