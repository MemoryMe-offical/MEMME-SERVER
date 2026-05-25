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
import memme.memoryme.upload.application.service.S3ObjectUrlBuilder;
import memme.memoryme.upload.application.service.UploadService;
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
    private final S3ObjectUrlBuilder objectUrlBuilder;
    private final UploadService uploadService;

    @Override
    @Transactional
    public BoardDto createBoard(CreateBoardRequest request) {
        UUID userUid = currentUserProvider.getUid();
        validateUserExists(userUid);
        if (request == null) {
            throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
        }
        validateBoardTitle(request.title());

        Board board = Board.builder()
                .uid(UUID.randomUUID())
                .userUid(userUid)
                .title(request.title().trim())
                .description(blankToNull(request.description()))
                .tags(normalizeTags(request.tags()))
                .bookmarked(false)
                .build();

        return BoardDto.from(boardRepository.saveAndFlush(board), this::resolveAttachmentUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDto getBoard(UUID boardUid) {
        return BoardDto.from(getCurrentUserBoard(boardUid), this::resolveAttachmentUrl);
    }

    @Override
    @Transactional
    public BoardDto updateBoard(UUID boardUid, UpdateBoardRequest request) {
        if (request == null) {
            throw new BusinessException(BoardErrorCode.INVALID_BOARD_REQUEST);
        }
        Board board = getCurrentUserBoard(boardUid);
        validateBoardTitle(request.title());

        board.updateMeta(
                request.title().trim(),
                blankToNull(request.description()),
                normalizeTags(request.tags())
        );
        boardRepository.flush();
        return BoardDto.from(board, this::resolveAttachmentUrl);
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
        board.changeBookmarked(request != null && request.valueOrFalse());
        boardRepository.flush();
        return BoardDto.from(board, this::resolveAttachmentUrl);
    }

    @Override
    @Transactional
    public NoteDto createNote(UUID boardUid, CreateNoteRequest request) {
        if (request == null) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
        }
        Board board = getCurrentUserBoard(boardUid);
        validateNoteTitle(request.title());

        OgDataDto ogData = resolveOgData(request.ogDatas(), request.ogData());
        List<String> urls = resolveUrls(request.urls(), request.url());
        Note note = Note.builder()
                .uid(UUID.randomUUID())
                .title(request.title().trim())
                .content(blankToNull(request.content()))
                .urls(urls)
                .url(firstUrl(urls))
                .ogTitle(ogData != null ? blankToNull(ogData.title()) : null)
                .ogDescription(ogData != null ? blankToNull(ogData.description()) : null)
                .ogImageUrl(ogData != null ? blankToNull(ogData.imageUrl()) : null)
                .ogSiteName(ogData != null ? blankToNull(ogData.siteName()) : null)
                .ogSummary(ogData != null ? blankToNull(ogData.summary()) : null)
                .build();

        note.replaceAttachments(toAttachments(board.getUserUid(), request.imageUris(), request.imageKeys(), request.videoUris(), request.videoKeys(), request.files()));
        board.addNote(note);
        boardRepository.flush();
        return NoteDto.from(note, this::resolveAttachmentUrl);
    }

    @Override
    @Transactional
    public NoteDto updateNote(UUID boardUid, UUID noteUid, UpdateNoteRequest request) {
        if (request == null) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
        }
        Board board = getCurrentUserBoard(boardUid);
        Note note = findNote(board, noteUid);
        validateNoteTitle(request.title());

        OgDataDto ogData = resolveOgData(request.ogDatas(), request.ogData());
        note.update(
                request.title().trim(),
                blankToNull(request.content()),
                resolveUrls(request.urls(), request.url()),
                ogData != null ? blankToNull(ogData.title()) : null,
                ogData != null ? blankToNull(ogData.description()) : null,
                ogData != null ? blankToNull(ogData.imageUrl()) : null,
                ogData != null ? blankToNull(ogData.siteName()) : null,
                ogData != null ? blankToNull(ogData.summary()) : null
        );
        note.replaceAttachments(toAttachments(board.getUserUid(), request.imageUris(), request.imageKeys(), request.videoUris(), request.videoKeys(), request.files()));
        board.touch();
        boardRepository.flush();
        return NoteDto.from(note, this::resolveAttachmentUrl);
    }

    @Override
    @Transactional
    public void deleteNote(UUID boardUid, UUID noteUid) {
        Board board = getCurrentUserBoard(boardUid);
        Note note = findNote(board, noteUid);
        board.removeNote(note);
        noteRepository.delete(note);
        boardRepository.flush();
    }

    @Override
    @Transactional
    public MoveNoteResponse moveNotes(UUID sourceBoardUid, MoveNoteRequest request) {
        return moveNotes(sourceBoardUid, null, request);
    }

    @Override
    @Transactional
    public MoveNoteResponse moveNote(UUID sourceBoardUid, UUID noteUid, MoveNoteRequest request) {
        return moveNotes(sourceBoardUid, noteUid, request);
    }

    private MoveNoteResponse moveNotes(UUID sourceBoardUid, UUID pathNoteUid, MoveNoteRequest request) {
        UUID targetBoardUid = request == null ? null : request.resolvedTargetBoardUid();
        List<UUID> noteUids = request == null ? List.of() : request.resolvedNoteUids(pathNoteUid);
        if (targetBoardUid == null) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
        }
        if (noteUids.isEmpty()) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
        }
        if (sourceBoardUid.equals(targetBoardUid)) {
            throw new BusinessException(BoardErrorCode.SAME_BOARD_MOVE_NOT_ALLOWED);
        }

        Board sourceBoard = getCurrentUserBoard(sourceBoardUid);
        Board targetBoard = getCurrentUserBoard(targetBoardUid);
        List<Note> notes = noteUids.stream()
                .map(noteUid -> findNote(sourceBoard, noteUid))
                .toList();

        notes.forEach(note -> {
            sourceBoard.removeNote(note);
            targetBoard.addNote(note);
        });
        boardRepository.flush();

        return new MoveNoteResponse(
                noteUids,
                noteUids.size(),
                BoardDto.from(sourceBoard, this::resolveAttachmentUrl),
                BoardDto.from(targetBoard, this::resolveAttachmentUrl)
        );
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

    private List<NoteAttachment> toAttachments(
            UUID userUid,
            List<String> imageUris,
            List<String> imageKeys,
            List<String> videoUris,
            List<String> videoKeys,
            List<FileAttachmentDto> files
    ) {
        List<NoteAttachment> attachments = new ArrayList<>();

        addUrlAttachments(attachments, userUid, AttachmentType.IMAGE, imageUris, null);
        addKeyAttachments(attachments, userUid, AttachmentType.IMAGE, imageKeys);
        addUrlAttachments(attachments, userUid, AttachmentType.VIDEO, videoUris, null);
        addKeyAttachments(attachments, userUid, AttachmentType.VIDEO, videoKeys);

        if (files != null) {
            files.stream()
                    .filter(file -> file != null && ((file.url() != null && !file.url().isBlank()) || (file.key() != null && !file.key().isBlank())))
                    .map(file -> normalizeFileAttachment(file, userUid))
                    .forEach(attachments::add);
        }

        long imageCount = attachments.stream().filter(attachment -> attachment.getType() == AttachmentType.IMAGE).count();
        if (imageCount > 10) {
            throw new BusinessException(BoardErrorCode.INVALID_NOTE_REQUEST);
        }

        return attachments;
    }

    private void addUrlAttachments(List<NoteAttachment> attachments, UUID userUid, AttachmentType type, List<String> urls, String originalName) {
        if (urls == null) {
            return;
        }
        urls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> {
                    String value = url.trim();
                    String key = extractKey(value);
                    return NoteAttachment.builder()
                        .uid(UUID.randomUUID())
                        .userUid(userUid)
                        .type(type)
                        .originalName(originalName)
                        .url(key == null ? value : objectUrlBuilder.build(key))
                        .s3Key(key)
                        .build();
                })
                .forEach(attachments::add);
    }

    private void addKeyAttachments(List<NoteAttachment> attachments, UUID userUid, AttachmentType type, List<String> keys) {
        if (keys == null) {
            return;
        }
        keys.stream()
                .filter(key -> key != null && !key.isBlank())
                .map(key -> key.trim())
                .map(key -> NoteAttachment.builder()
                        .uid(UUID.randomUUID())
                        .userUid(userUid)
                        .type(type)
                        .url(objectUrlBuilder.build(key))
                        .s3Key(key)
                        .build())
                .forEach(attachments::add);
    }

    private NoteAttachment normalizeFileAttachment(FileAttachmentDto file, UUID userUid) {
        String key = blankToNull(file.key());
        String url = blankToNull(file.url());
        if (url == null && key != null) {
            url = objectUrlBuilder.build(key);
        }
        if (key == null && url != null) {
            key = extractKey(url);
        }
        if (key != null) {
            url = objectUrlBuilder.build(key);
        }
        return NoteAttachment.builder()
                .uid(file.uid() != null ? file.uid() : UUID.randomUUID())
                .userUid(userUid)
                .type(AttachmentType.FILE)
                .originalName(blankToNull(file.name()))
                .url(url)
                .s3Key(key)
                .mimeType(blankToNull(file.mimeType()))
                .sizeBytes(file.size())
                .thumbnailUrl(blankToNull(file.thumbnailUrl()))
                .durationSeconds(file.duration())
                .build();
    }

    private String extractKeyFromObjectUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String marker = "key=";
        int index = url.indexOf(marker);
        if (index < 0) {
            return null;
        }
        String encodedKey = url.substring(index + marker.length());
        int ampIndex = encodedKey.indexOf('&');
        if (ampIndex >= 0) {
            encodedKey = encodedKey.substring(0, ampIndex);
        }
        return java.net.URLDecoder.decode(encodedKey, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String extractKey(String value) {
        String key = extractKeyFromObjectUrl(value);
        if (key != null) {
            return key;
        }
        String normalized = blankToNull(value);
        return isS3ObjectKey(normalized) ? normalized : null;
    }

    private boolean isS3ObjectKey(String value) {
        return value != null
                && !value.startsWith("/")
                && !value.startsWith("http://")
                && !value.startsWith("https://")
                && value.contains("/users/")
                && (value.contains("/images/") || value.contains("/videos/") || value.contains("/files/"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<String> resolveUrls(List<String> urls, String url) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (urls != null) {
            for (String u : urls) {
                if (u != null && !u.isBlank()) {
                    normalized.add(u.trim());
                }
            }
        }
        String legacyUrl = blankToNull(url);
        if (normalized.isEmpty() && legacyUrl != null) {
            normalized.add(legacyUrl);
        }
        return new ArrayList<>(normalized);
    }

    private String firstUrl(List<String> urls) {
        return urls == null || urls.isEmpty() ? null : urls.get(0);
    }

    private OgDataDto resolveOgData(List<OgDataDto> ogDatas, OgDataDto ogData) {
        if (ogDatas != null && !ogDatas.isEmpty()) {
            return ogDatas.get(0);
        }
        return ogData;
    }

    private String resolveAttachmentUrl(NoteAttachment attachment) {
        String key = blankToNull(attachment.getS3Key());
        if (key == null) {
            key = extractKey(attachment.getUrl());
        }
        if (key == null) {
            return attachment.getUrl();
        }
        return uploadService.createReadUrl(key);
    }
}
