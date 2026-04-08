package memme.memoryme.memo.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.memo.api.controller.api.MemoApi;
import memme.memoryme.memo.api.dto.memo.NewMemoDto;
import memme.memoryme.memo.api.dto.memo.MemoDto;
import memme.memoryme.memo.application.service.MemoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NoteController implements MemoApi {
    private final MemoService memoService;

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> createMemo(NewMemoDto newNote) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(
                    201,
                    "생성 성공",
                    memoService.createMemo(newNote)
                )
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> updateMemo(MemoDto memoDto) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseWrapper.ok(
                        200,
                        "수정 성공",
                        memoService.updateMemo(memoDto)
                )
        );
    }
}
