package memme.memoryme.note.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import memme.memoryme.global.docs.response.NoteResponse;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.note.api.dto.note.NewNoteDto;
import memme.memoryme.note.api.dto.note.NoteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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
                                    schema = @Schema(implementation = NoteResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(
                            schema = @Schema(implementation = ResponseWrapper.class)
                    )),
                    @ApiResponse(responseCode = "404", description = "데이터 없음", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
            }
    )
    @PostMapping
    // todo: Token에서 UUID 뽑아내어 사용
    ResponseEntity<ResponseWrapper<NoteDto>> toNoteDto(
            @Parameter(description = "새로운 메모")
            @RequestBody NewNoteDto newNote
    );

    @Operation(
            summary = "메모 수정",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(
                                    schema = @Schema(implementation = NoteResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(
                            schema = @Schema(implementation = ResponseWrapper.class)
                    )),
                    @ApiResponse(responseCode = "404", description = "데이터 없음", content = @Content),
                    @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
            }
    )
    @PatchMapping
        // todo: Token에서 UUID 뽑아내어 사용
    ResponseEntity<ResponseWrapper<NoteDto>> updateNote(
            @Parameter(description = "수정 할 메모")
            @RequestBody NoteDto noteDto
    );
}
