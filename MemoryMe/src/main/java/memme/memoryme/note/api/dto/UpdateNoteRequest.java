package memme.memoryme.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "노트 수정 요청 DTO")
public record UpdateNoteRequest(
        @Schema(description = "노트 제목", example = "수정된 노트 제목")
        String title,
        @Schema(description = "노트 본문", example = "수정된 내용")
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
