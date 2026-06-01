package memme.memoryme.memo.api.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "메모를 기존 보드의 노트로 변환하는 요청 DTO")
public record ConvertMemoToExistingBoardRequest(
        @Schema(description = "노트 제목. 비우면 메모 내용이 사용됩니다.", example = "코딩 공부하기")
        String noteTitle,
        @Schema(description = "노트 본문")
        String content,
        @Schema(description = "생성되는 노트 생성 시각. 생략하면 서버 현재 시각이 사용됩니다.", example = "2026-06-01T10:30:00")
        LocalDateTime createdAt
) {
}
