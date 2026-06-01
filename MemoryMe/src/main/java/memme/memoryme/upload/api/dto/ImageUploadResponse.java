package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이미지 업로드 응답 DTO")
public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지 원본 파일명 목록", example = "[\"photo.jpg\"]")
        List<String> names,
        @Schema(description = "업로드된 이미지 접근 URL 목록", example = "[\"/v1/upload/object?key=prod/memme/users/user-uid/images/image.webp\"]")
        List<String> urls,
        @Schema(description = "S3 객체 key 목록", example = "[\"prod/memme/users/user-uid/images/image.webp\"]")
        List<String> keys
) {
}
