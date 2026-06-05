package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "업로드 객체 DTO")
public record UploadObjectDto(
        @Schema(description = "업로드 객체 카테고리", example = "images")
        String category,
        @Schema(description = "원본 파일명", example = "photo.jpg")
        String name,
        @Schema(description = "객체 접근 URL", example = "https://s3-presigned-url...")
        String url,
        @Schema(description = "S3 객체 key", example = "prod/memme/users/user-uid/images/image.webp")
        String key,
        @Schema(description = "파일 크기(bytes)", example = "204800")
        Long size,
        @Schema(description = "마지막 수정 시각", example = "2026-05-20T05:30:00Z")
        Instant lastModified
) {
}
