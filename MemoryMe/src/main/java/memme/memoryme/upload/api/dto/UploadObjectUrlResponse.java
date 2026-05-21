package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업로드 객체 접근 URL 응답 DTO")
public record UploadObjectUrlResponse(
        @Schema(description = "S3 presigned URL", example = "https://s3-presigned-url...")
        String url
) {
}
