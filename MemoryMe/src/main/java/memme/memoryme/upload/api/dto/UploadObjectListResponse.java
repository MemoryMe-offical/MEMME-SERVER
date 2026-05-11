package memme.memoryme.upload.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "업로드 객체 목록 응답 DTO")
public record UploadObjectListResponse(
        @Schema(description = "업로드 객체 목록")
        List<UploadObjectDto> items,
        @Schema(description = "전체 개수", example = "12")
        long total
) {
}
