package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메모 생성 요청 DTO")
public record NewMemoDto(
        @Schema(description = "빠르게 남기는 메모 내용", example = "코딩 공부하기")
        String text,
        @Schema(description = "기존 프론트 호환용 메모 내용", example = "코딩 공부하기")
        String title,
        @Schema(description = "기존 프론트 호환용 메모 내용", example = "코딩 공부하기")
        String content
) {
    public String resolvedText() {
        if (text != null && !text.isBlank()) {
            return text;
        }
        if (title != null && !title.isBlank()) {
            return title;
        }
        return content;
    }
}
