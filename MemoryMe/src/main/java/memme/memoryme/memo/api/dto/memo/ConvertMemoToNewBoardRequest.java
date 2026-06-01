package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "메모를 새 보드로 변환하는 요청 DTO")
public record ConvertMemoToNewBoardRequest(
        @Schema(description = "새 보드 제목", example = "백엔드 공부")
        String boardTitle,
        @Schema(description = "새 보드 설명", example = "Spring과 JPA 공부 모음")
        String description,
        @Schema(description = "새 보드 태그 목록")
        List<String> tags,
        @Schema(description = "첫 노트 제목. 비우면 메모 내용이 사용됩니다.", example = "코딩 공부하기")
        String noteTitle,
        @Schema(description = "첫 노트 본문")
        String content,
        @Schema(description = "새 보드와 첫 노트 생성 시각. 생략하면 서버 현재 시각이 사용됩니다.", example = "2026-06-01T10:30:00")
        LocalDateTime createdAt
) {
}
