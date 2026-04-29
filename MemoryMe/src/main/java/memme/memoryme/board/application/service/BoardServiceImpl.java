package memme.memoryme.board.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.api.dto.*;
import memme.memoryme.board.domain.Board;
import memme.memoryme.board.exception.BoardErrorCode;
import memme.memoryme.board.infra.repository.BoardRepository;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.memo.application.port.UserReader;
import memme.memoryme.note.api.dto.*;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;
import memme.memoryme.note.infra.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final NoteRepository noteRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserReader userReader;

    @Override
    @Transactional
    public BoardDto createBoard(CreateBoardRequest request) {
        UUID userUid = currentUserProvider.getUid();
        validateUserExists(userUid);
        validateBoardTitle(request.title());

        Board board = Board.builder()
                .uid(UUID.randomUUID())
                .userUid(userUid)
                .title(request.title().trim())
                .description(blankToNull(request.description()))
                .tags(normalizeTags(request.tags()))
                .bookmarked(false)
                .build();

        return BoardDto.from(boardRepository.save(board));
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDto getBoard(UUID boardUid) {
        Board board = getCurrentUserBoard(boardUid);
        return BoardDto.from(board);
    }

    @Override
    @Transactional
    public BoardDto updateBoard(UUID boardUid, UpdateBoardRequest request) {
        Board board = getCurrentUserBoard(boardUid);
        validateBoardTitle(request.title());

        board.updateMeta(
                request.title().trim(),
                blankToNull(request.description()),
                normalizeTags(request.tags())
        );
        return BoardDto.from(board);
    }

    @Override
    @Transactional
    public void deleteBoard(UUID boardUid) {
        UUID userUid = currentUserProvider.getUid();
        Board board = boardRepository.findByUidAndUserUid(boardUid, userUid)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));
        boardRepository.delete(board);
    }

    @Override
    @Transactional
    public BoardDto updateBookmark(UUID boardUid, BoardBookmarkRequest request) {
        Board board = getCurrentUserBoard(boardUid);
        board.changeBookmarked(request.valueOrFalse());
        return BoardDto.from(board);
    }

    @Override
    @Transactional
    public NoteDto createNote(UUID boardUid, CreateNoteRequest request) {
        Board board = getCurrentUserBoard(boardUid);
        validateNoteTitle(request.title());

        Note note = Note.builder()
                .uid(UUID.randomUUID())
                .title(request.title().trim())
                .content(blankToNull(request.content()))
                .url(blankToNull(request.url()))
                .ogTitle(request.ogData() != null ? blankToNull(request.ogData().title()) : null)
                .ogDescription(request.ogData() != null ? blankToNull(request.ogData().description()) : null)
                .ogImageUrl(request.ogData() != null ? blankToNull(request.ogData().imageUrl()) : null)
                .ogSiteName(request.ogData() != null ? blankToNull(request.ogData().siteName()) : null)
                .sortOrder(board.nextSortOrder())
                .build();

        note.replaceAttachments(toAttachments(request.imageUris(), request.videoUris(), request.files()));
        board.addNote(note);
        return NoteDto.from(note);
    }

    @Override
    @Transactional
    public NoteDto updateNote(UUID boardUid, UUID noteUid, UpdateNoteRequest request) {
        Board board = getCurrentUserBoard(boardUid);
        Note note = findNote(board, noteUid);
        validateNoteTitle(request.title());

        note.update(
                request.title().trim(),
                blankToNull(request.content()),
                blankToNull(request.url()),
                request.ogData() != null ? blankToNull(request.ogData().title()) : null,
                request.ogData() != null ? blankToNull(request.ogData().description()) : null,
                request.ogData() != null ? blankToNull(request.ogData().imageUrl()) : null,
                request.ogData() != null ? blankToNull(request.ogData().siteName()) : null
        );
        note.replaceAttachments(toAttachments(request.imageUris(), request.videoUris(), request.files()));
        board.touch();
        return NoteDto.from(note);
    }

    @Override
    @Transactional
    public void deleteNote(UUID boardUid, UUID noteUid) {
        Board board = getCurrentUserBoard(boardUid);
        Note note = findNote(board, noteUid);
        board.removeNote(note);
        noteRepository.delete(note);
    }

    @Override
    @Transactional
    public MoveNoteResponse moveNote(UUID sourceBoardUid, UUID noteUid, MoveNoteRequest request) {
        if (request.targetBoardUid() == null) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
        }
        if (sourceBoardUid.equals(request.targetBoardUid())) {
            throw new BusinessException(BoardErrorCode.SAME_BOARD_MOVE_NOT_ALLOWED);
        }

        Board sourceBoard = getCurrentUserBoard(sourceBoardUid);
        Board targetBoard = getCurrentUserBoard(request.targetBoardUid());
        Note note = findNote(sourceBoard, noteUid);

        sourceBoard.removeNote(note);
        targetBoard.addNote(note);

        return new MoveNoteResponse(BoardDto.from(sourceBoard), BoardDto.from(targetBoard));
    }

    private Board getCurrentUserBoard(UUID boardUid) {
        UUID userUid = currentUserProvider.getUid();
        return boardRepository.findByUidAndUserUid(boardUid, userUid)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));
    }

    private Note findNote(Board board, UUID noteUid) {
        return board.getNotes().stream()
                .filter(note -> note.getUid().equals(noteUid))
                .findFirst()
                .orElseThrow(() -> new BusinessException(BoardErrorCode.NOTE_NOT_FOUND));
    }

    private void validateUserExists(UUID userUid) {
        if (!userReader.existsByUid(userUid)) {
            throw new BusinessException(BoardErrorCode.USER_UID_NOT_FOUND);
        }
    }

    private void validateBoardTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 100) {
            throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
        }
    }

    private void validateNoteTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 100) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
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

    private List<NoteAttachment> toAttachments(List<String> imageUris, List<String> videoUris, List<FileAttachmentDto> files) {
        List<NoteAttachment> attachments = new ArrayList<>();

        if (imageUris != null) {
            if (imageUris.size() > 10) {
                throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
            }
            imageUris.stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> NoteAttachment.builder()
                            .uid(UUID.randomUUID())
                            .type(AttachmentType.IMAGE)
                            .url(url.trim())
                            .build())
                    .forEach(attachments::add);
        }

        if (videoUris != null) {
            videoUris.stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> NoteAttachment.builder()
                            .uid(UUID.randomUUID())
                            .type(AttachmentType.VIDEO)
                            .url(url.trim())
                            .build())
                    .forEach(attachments::add);
        }

        if (files != null) {
            files.stream()
                    .filter(file -> file != null && file.url() != null && !file.url().isBlank())
                    .map(file -> file.toEntity(AttachmentType.FILE))
                    .forEach(attachments::add);
        }

        return attachments;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
