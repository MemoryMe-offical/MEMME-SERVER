package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영상 업로드 응답 DTO")
public record VideoUploadResponse(
        @Schema(description = "영상 접근 URL")
        String url,
        @Schema(description = "S3 객체 key")
        String key,
        @Schema(description = "썸네일 URL")
        String thumbnailUrl,
        @Schema(description = "영상 길이(초)")
        Integer duration,
        @Schema(description = "파일 크기(bytes)")
        Long size
) {
}
