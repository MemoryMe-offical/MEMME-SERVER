package memme.memoryme.memo.application.service;

import memme.memoryme.memo.api.dto.memo.NewMemoDto;
import memme.memoryme.memo.api.dto.memo.MemoDto;

public interface MemoService {
    MemoDto createMemo(NewMemoDto newMemoDto);
    MemoDto updateMemo(MemoDto memoDto);
}
