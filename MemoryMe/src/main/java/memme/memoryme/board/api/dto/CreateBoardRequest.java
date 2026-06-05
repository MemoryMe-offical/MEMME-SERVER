package memme.memoryme.board.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "보드 생성 요청 DTO")
public record CreateBoardRequest(
        @Schema(description = "보드 제목", example = "수학교육 과동아리")
        String title,
        @Schema(description = "보드 설명", example = "동아리 관련 공지와 자료 모음")
        String description,
        @Schema(description = "태그 목록", example = "[\"동아리\", \"학교\"]")
        List<String> tags,
        @Schema(description = "보드 생성 시각. 생략하면 서버 현재 시각이 사용됩니다.", example = "2026-05-30T20:30:00")
        LocalDateTime createdAt
) {
}
