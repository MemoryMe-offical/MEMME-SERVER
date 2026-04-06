package memme.memoryme.note.api.dto.note;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "메모 생성 DTO")
public record NewNoteDto(
        // todo: 추후 삭제
        Long userId,

        @Schema(description = "내용", example = "오늘 회의 정리")
        String title
) {
}
