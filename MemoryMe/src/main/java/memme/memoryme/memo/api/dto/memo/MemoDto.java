package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import memme.memoryme.memo.domain.Memo;
import memme.memoryme.note.api.dto.FileAttachmentDto;
import memme.memoryme.note.api.dto.MediaAttachmentDto;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.NoteAttachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Schema(description = "메모 응답 DTO")
public record MemoDto(
        @Schema(description = "메모 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,

        @Schema(description = "타임라인 타입", example = "memo")
        String type,

        @Schema(description = "메모 내용", example = "코딩 공부하기")
        String text,

        @Schema(description = "북마크 여부", example = "false")
        boolean bookmarked,

        @Schema(description = "첨부 이미지 URL 목록", example = "[\"https://s3-presigned-url...\"]")
        List<String> imageUris,
        @Schema(description = "첨부 이미지 S3 key 목록", example = "[\"prod/memme/users/user-uid/images/image.webp\"]")
        List<String> imageKeys,
        @Schema(description = "첨부 이미지 목록")
        List<MediaAttachmentDto> images,
        @Schema(description = "첨부 영상 URL 목록", example = "[\"https://s3-presigned-url...\"]")
        List<String> videoUris,
        @Schema(description = "첨부 영상 S3 key 목록", example = "[\"prod/memme/users/user-uid/videos/video.mp4\"]")
        List<String> videoKeys,
        @Schema(description = "첨부 영상 목록")
        List<MediaAttachmentDto> videos,
        @Schema(description = "첨부 파일 목록")
        List<FileAttachmentDto> files,

        @Schema(description = "생성 시각", example = "2026-04-11T10:00:00")
        LocalDateTime createdAt
) {
    public static MemoDto from(Memo memo) {
        return from(memo, null);
    }

    public static MemoDto from(Memo memo, Function<NoteAttachment, String> urlResolver) {
        return new MemoDto(
                memo.getUid(),
                "memo",
                memo.getText(),
                memo.isBookmarked(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.IMAGE)
                        .map(attachment -> resolveUrl(attachment, urlResolver))
                        .toList(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.IMAGE)
                        .map(MemoDto::resolveKey)
                        .toList(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.IMAGE)
                        .map(attachment -> MediaAttachmentDto.from(attachment, urlResolver))
                        .toList(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.VIDEO)
                        .map(attachment -> resolveUrl(attachment, urlResolver))
                        .toList(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.VIDEO)
                        .map(MemoDto::resolveKey)
                        .toList(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.VIDEO)
                        .map(attachment -> MediaAttachmentDto.from(attachment, urlResolver))
                        .toList(),
                memo.getAttachments().stream()
                        .filter(attachment -> attachment.getType() == AttachmentType.FILE)
                        .map(attachment -> FileAttachmentDto.from(attachment, urlResolver))
                        .toList(),
                memo.getCreatedAt()
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
