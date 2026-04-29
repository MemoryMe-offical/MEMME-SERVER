package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "파일 업로드 응답 DTO")
public record FileUploadResponse(
        @Schema(description = "파일 UID")
        UUID uid,
        @Schema(description = "원본 파일명")
        String name,
        @Schema(description = "파일 URL")
        String url,
        @Schema(description = "MIME 타입")
        String mimeType,
        @Schema(description = "파일 크기(bytes)")
        Long size
) {
}
