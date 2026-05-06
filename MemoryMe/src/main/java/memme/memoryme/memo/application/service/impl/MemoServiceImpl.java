package memme.memoryme.memo.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.board.domain.Board;
import memme.memoryme.board.exception.BoardErrorCode;
import memme.memoryme.board.infra.repository.BoardRepository;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.memo.api.dto.memo.*;
import memme.memoryme.memo.application.port.UserReader;
import memme.memoryme.memo.application.service.MemoService;
import memme.memoryme.memo.domain.Memo;
import memme.memoryme.memo.exception.MemoErrorCode;
import memme.memoryme.memo.infra.repository.MemoRepository;
import memme.memoryme.note.domain.Note;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService {
    private final MemoRepository memoRepository;
    private final BoardRepository boardRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserReader userReader;

    @Override
    @Transactional
    public MemoDto createMemo(NewMemoDto request) {
        UUID userUid = currentUserProvider.getUid();
        validateUserExists(userUid);
        String text = request == null ? null : request.resolvedText();
        validateText(text);

        Memo memo = Memo.builder()
                .uid(UUID.randomUUID())
                .userUid(userUid)
                .text(text.trim())
                .bookmarked(false)
                .build();

        return MemoDto.from(memoRepository.saveAndFlush(memo));
    }

    @Override
    @Transactional
    public void deleteMemo(UUID memoUid) {
        UUID userUid = currentUserProvider.getUid();
        Memo memo = memoRepository.findByUidAndUserUid(memoUid, userUid)
                .orElseThrow(() -> new BusinessException(MemoErrorCode.MEMO_NOT_FOUND));
        memoRepository.delete(memo);
    }

    @Override
    @Transactional
    public MemoDto updateBookmark(UUID memoUid, BookmarkRequest request) {
        UUID userUid = currentUserProvider.getUid();
        Memo memo = memoRepository.findByUidAndUserUid(memoUid, userUid)
                .orElseThrow(() -> new BusinessException(MemoErrorCode.MEMO_NOT_FOUND));

        memo.changeBookmarked(request != null && request.valueOrFalse());
        return MemoDto.from(memo);
    }

    @Override
    @Transactional
    public BoardDto convertToNewBoard(UUID memoUid, ConvertMemoToNewBoardRequest request) {
        if (request == null) {
            throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
        }

        UUID userUid = currentUserProvider.getUid();
        validateUserExists(userUid);
        Memo memo = getCurrentUserMemo(memoUid, userUid);
        validateBoardTitle(request.boardTitle());

        Board board = Board.builder()
                .uid(UUID.randomUUID())
                .userUid(userUid)
                .title(request.boardTitle().trim())
                .description(blankToNull(request.description()))
                .tags(normalizeTags(request.tags()))
                .bookmarked(false)
                .build();

        board.addNote(createNoteFromMemo(memo, request.noteTitle(), request.content()));
        Board savedBoard = boardRepository.saveAndFlush(board);
        memoRepository.delete(memo);
        memoRepository.flush();
        return BoardDto.from(savedBoard);
    }

    @Override
    @Transactional
    public BoardDto convertToExistingBoard(UUID memoUid, UUID boardUid, ConvertMemoToExistingBoardRequest request) {
        UUID userUid = currentUserProvider.getUid();
        Memo memo = getCurrentUserMemo(memoUid, userUid);
        Board board = boardRepository.findByUidAndUserUid(boardUid, userUid)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        String noteTitle = request == null ? null : request.noteTitle();
        String content = request == null ? null : request.content();
        board.addNote(createNoteFromMemo(memo, noteTitle, content));
        boardRepository.flush();
        memoRepository.delete(memo);
        memoRepository.flush();
        return BoardDto.from(board);
    }

    private Memo getCurrentUserMemo(UUID memoUid, UUID userUid) {
        return memoRepository.findByUidAndUserUid(memoUid, userUid)
                .orElseThrow(() -> new BusinessException(MemoErrorCode.MEMO_NOT_FOUND));
    }

    private Note createNoteFromMemo(Memo memo, String noteTitle, String content) {
        String title = blankToNull(noteTitle);
        if (title == null) {
            title = memo.getText();
        }
        if (title.length() > 100) {
            title = title.substring(0, 100);
        }
        return Note.builder()
                .uid(UUID.randomUUID())
                .title(title)
                .content(blankToNull(content))
                .build();
    }

    private void validateUserExists(UUID userUid) {
        if (!userReader.existsByUid(userUid)) {
            throw new BusinessException(MemoErrorCode.USER_UID_NOT_FOUND);
        }
    }

    private void validateText(String text) {
        if (text == null || text.isBlank() || text.length() > 2000) {
            throw new BusinessException(MemoErrorCode.INVALID_MEMO_REQUEST);
        }
    }

    private void validateBoardTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 100) {
            throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
        }
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new LinkedHashSet<>();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            String value = tag.trim().toLowerCase(Locale.ROOT);
            if (value.length() > 30) {
                throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
            }
            normalized.add(value);
            if (normalized.size() > 10) {
                throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
            }
        }
        return normalized;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
