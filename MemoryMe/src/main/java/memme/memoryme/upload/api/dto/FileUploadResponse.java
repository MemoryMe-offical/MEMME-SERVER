package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "파일 업로드 응답 DTO")
public record FileUploadResponse(
        @Schema(description = "파일 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "원본 파일명", example = "report.pdf")
        String name,
        @Schema(description = "파일 접근 URL", example = "/v1/upload/object?key=prod/memme/users/user-uid/files/report.pdf")
        String url,
        @Schema(description = "S3 객체 key", example = "prod/memme/users/user-uid/files/report.pdf")
        String key,
        @Schema(description = "MIME 타입", example = "application/pdf")
        String mimeType,
        @Schema(description = "파일 크기(bytes)", example = "204800")
        Long size
) {
}
