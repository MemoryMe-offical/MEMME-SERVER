package memme.memoryme.tag.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그 DTO")
public record TagDto(
        @Schema(description = "태그명", example = "동아리")
        String name,
        @Schema(description = "사용 횟수", example = "12")
        long count
) {
}
