package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "노트 생성 요청 DTO")
public record CreateNoteRequest(
        @Schema(description = "노트 제목", example = "여름 MT")
        String title,
        @Schema(description = "노트 본문", example = "MT 추가요금 공지...")
        String content,
        @Schema(description = "첨부 이미지 URL 목록")
        List<String> imageUris,
        @Schema(description = "첨부 이미지 S3 key 목록")
        List<String> imageKeys,
        @Schema(description = "첨부 영상 URL 목록")
        List<String> videoUris,
        @Schema(description = "첨부 영상 S3 key 목록")
        List<String> videoKeys,
        @Schema(description = "첨부 파일 목록")
        List<FileAttachmentDto> files,
        @Schema(description = "링크 URL")
        String url,
        @Schema(description = "OG 미리보기 데이터")
        OgDataDto ogData
) {
}
