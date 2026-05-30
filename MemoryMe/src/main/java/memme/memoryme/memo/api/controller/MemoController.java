package memme.memoryme.memo.api.controller;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.memo.api.controller.api.MemoApi;
import memme.memoryme.memo.api.dto.memo.*;
import memme.memoryme.memo.application.service.MemoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MemoController implements MemoApi {
    private final MemoService memoService;

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> createMemo(NewMemoDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "메모 생성 성공", memoService.createMemo(request))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> createImageMemo(String content, MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "이미지 메모 생성 성공", memoService.createImageMemo(content, file))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> createVideoMemo(String content, MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "영상 메모 생성 성공", memoService.createVideoMemo(content, file))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> createFileMemo(String content, MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "파일 메모 생성 성공", memoService.createFileMemo(content, file))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<Void>> deleteMemo(UUID memoUid) {
        memoService.deleteMemo(memoUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ResponseWrapper.ok(204, "메모 삭제 성공", null)
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<MemoDto>> updateBookmark(UUID memoUid, BookmarkRequest request) {
        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "메모 북마크 변경 성공", memoService.updateBookmark(memoUid, request))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<BoardDto>> convertToNewBoard(UUID memoUid, ConvertMemoToNewBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.ok(201, "메모를 새 보드로 변환 성공", memoService.convertToNewBoard(memoUid, request))
        );
    }

    @Override
    public ResponseEntity<ResponseWrapper<BoardDto>> convertToExistingBoard(UUID memoUid, UUID boardUid, ConvertMemoToExistingBoardRequest request) {
        return ResponseEntity.ok(
                ResponseWrapper.ok(200, "메모를 기존 보드에 추가 성공", memoService.convertToExistingBoard(memoUid, boardUid, request))
        );
    }
}
