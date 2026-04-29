package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보드 수정 요청 DTO")
public record UpdateBoardRequest(
        @Schema(description = "보드 제목", example = "수정된 제목")
        String title,
        @Schema(description = "보드 설명", example = "수정된 설명")
        String description,
        @Schema(description = "태그 목록", example = "[\"동아리\", \"학교\", \"공지\"]")
        List<String> tags
) {
}
