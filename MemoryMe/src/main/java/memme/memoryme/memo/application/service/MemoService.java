package memme.memoryme.memo.application.service;

import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.memo.api.dto.memo.*;

import java.util.UUID;

public interface MemoService {
    MemoDto createMemo(NewMemoDto request);
    void deleteMemo(UUID memoUid);
    MemoDto updateBookmark(UUID memoUid, BookmarkRequest request);
    BoardDto convertToNewBoard(UUID memoUid, ConvertMemoToNewBoardRequest request);
    BoardDto convertToExistingBoard(UUID memoUid, UUID boardUid, ConvertMemoToExistingBoardRequest request);
}
