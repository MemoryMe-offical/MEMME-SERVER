package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이미지 업로드 응답 DTO")
public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지 URL 목록")
        List<String> urls
) {
}
