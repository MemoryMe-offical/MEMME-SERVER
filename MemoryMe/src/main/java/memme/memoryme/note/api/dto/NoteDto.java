package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Schema(description = "노트 응답 DTO")
public record NoteDto(
        @Schema(description = "노트 UID")
        UUID uid,
        @Schema(description = "노트 제목")
        String title,
        @Schema(description = "노트 본문")
        String content,
        @Schema(description = "첨부 이미지 URL 목록")
        List<String> imageUris,
        @Schema(description = "첨부 이미지 S3 key 목록")
        List<String> imageKeys,
        @Schema(description = "첨부 이미지 목록")
        List<MediaAttachmentDto> images,
        @Schema(description = "첨부 영상 URL 목록")
        List<String> videoUris,
        @Schema(description = "첨부 영상 S3 key 목록")
        List<String> videoKeys,
        @Schema(description = "첨부 영상 목록")
        List<MediaAttachmentDto> videos,
        @Schema(description = "첨부 파일 목록")
        List<FileAttachmentDto> files,
        @Schema(description = "링크 URL")
        String url,
        @Schema(description = "OG 미리보기 데이터")
        OgDataDto ogData,
        @Schema(description = "생성 시각")
        LocalDateTime createdAt,
        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {
    public static NoteDto from(Note note) {
        return from(note, null);
    }

    public static NoteDto from(Note note, Function<NoteAttachment, String> urlResolver) {
        return new NoteDto(
                note.getUid(),
                note.getTitle(),
                note.getContent(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.IMAGE)
                        .map(attachment -> resolveUrl(attachment, urlResolver))
                        .toList(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.IMAGE)
                        .map(NoteDto::resolveKey)
                        .toList(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.IMAGE)
                        .map(attachment -> MediaAttachmentDto.from(attachment, urlResolver))
                        .toList(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.VIDEO)
                        .map(attachment -> resolveUrl(attachment, urlResolver))
                        .toList(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.VIDEO)
                        .map(NoteDto::resolveKey)
                        .toList(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.VIDEO)
                        .map(attachment -> MediaAttachmentDto.from(attachment, urlResolver))
                        .toList(),
                note.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.FILE)
                        .map(attachment -> FileAttachmentDto.from(attachment, urlResolver))
                        .toList(),
                note.getUrl(),
                OgDataDto.from(note),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private static String resolveUrl(NoteAttachment attachment, Function<NoteAttachment, String> urlResolver) {
        return urlResolver == null ? attachment.getUrl() : urlResolver.apply(attachment);
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
