package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메모 생성 요청 DTO")
public record NewMemoDto(
        @Schema(description = "빠르게 남기는 메모 내용", example = "코딩 공부하기")
        String text
) {
}
