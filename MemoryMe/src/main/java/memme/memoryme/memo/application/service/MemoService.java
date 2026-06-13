package memme.memoryme.memo.application.service;

import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.memo.api.dto.memo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MemoService {
    MemoDto createMemo(NewMemoDto request);
    MemoDto createImageMemo(String content, MultipartFile file);
    MemoDto createImageMemo(String content, List<MultipartFile> files);
    MemoDto createVideoMemo(String content, MultipartFile file);
    MemoDto createFileMemo(String content, MultipartFile file);
    void deleteMemo(UUID memoUid);
    void deleteAllByUserUid(UUID userUid);
    MemoDto updateBookmark(UUID memoUid, BookmarkRequest request);
    BoardDto convertToNewBoard(UUID memoUid, ConvertMemoToNewBoardRequest request);
    BoardDto convertToExistingBoard(UUID memoUid, UUID boardUid, ConvertMemoToExistingBoardRequest request);
}
