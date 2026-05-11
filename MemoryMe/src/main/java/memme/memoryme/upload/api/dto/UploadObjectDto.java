package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "업로드 객체 DTO")
public record UploadObjectDto(
        @Schema(description = "업로드 객체 카테고리", example = "images")
        String category,
        @Schema(description = "객체 접근 URL", example = "/v1/upload/object?key=memme/users/.../image.png")
        String url,
        @Schema(description = "S3 객체 key")
        String key,
        @Schema(description = "파일 크기(bytes)")
        Long size,
        @Schema(description = "마지막 수정 시각")
        Instant lastModified
) {
}
