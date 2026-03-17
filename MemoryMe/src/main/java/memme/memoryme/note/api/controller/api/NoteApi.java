package memme.memoryme.note.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;
import memme.memoryme.note.domain.Note;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Note API", description = "메모 API")
@RequestMapping("/v1")
public interface NoteApi {
    @Operation(
            summary = "메모 생성",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = NewNoteDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "데이터 없음", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // todo: Header로 UUID 받아오는 느낌으로 변경 해야 할 것 같음
    ResponseEntity<ResponseWrapper<NoteDto>> toNoteDto(
            @Parameter(description = "새로운 메모")
            @RequestBody NewNoteDto newNote);
}
