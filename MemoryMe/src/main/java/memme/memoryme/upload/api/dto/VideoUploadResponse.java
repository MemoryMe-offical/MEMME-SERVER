package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영상 업로드 응답 DTO")
public record VideoUploadResponse(
        @Schema(description = "영상 접근 URL", example = "/v1/upload/object?key=prod/memme/users/user-uid/videos/video.mp4")
        String url,
        @Schema(description = "S3 객체 key", example = "prod/memme/users/user-uid/videos/video.mp4")
        String key,
        @Schema(description = "썸네일 URL", example = "https://example.com/thumb.jpg")
        String thumbnailUrl,
        @Schema(description = "영상 길이(초)", example = "42")
        Integer duration,
        @Schema(description = "파일 크기(bytes)", example = "10485760")
        Long size
) {
}
