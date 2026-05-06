package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이미지 업로드 응답 DTO")
public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지 접근 URL 목록")
        List<String> urls,
        @Schema(description = "S3 객체 key 목록")
        List<String> keys
) {
}
