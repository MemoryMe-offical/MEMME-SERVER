package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "메모 생성 DTO")
public record NewMemoDto(
        @Schema(description = "내용", example = "오늘 회의 정리")
        String title
) {
}
