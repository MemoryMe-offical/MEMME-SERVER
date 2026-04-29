package memme.memoryme.tag.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "태그 목록 응답 DTO")
public record TagListResponse(
        @Schema(description = "태그 목록")
        List<TagDto> tags
) {
}
