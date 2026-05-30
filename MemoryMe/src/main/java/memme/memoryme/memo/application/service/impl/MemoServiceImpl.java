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
import memme.memoryme.note.api.dto.FileAttachmentDto;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;
import memme.memoryme.upload.api.dto.FileUploadResponse;
import memme.memoryme.upload.api.dto.ImageUploadResponse;
import memme.memoryme.upload.api.dto.VideoUploadResponse;
import memme.memoryme.upload.application.service.S3ObjectUrlBuilder;
import memme.memoryme.upload.application.service.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final S3ObjectUrlBuilder objectUrlBuilder;
    private final UploadService uploadService;

    @Override
    @Transactional
    public MemoDto createMemo(NewMemoDto request) {
        UUID userUid = currentUserProvider.getUid();
        validateUserExists(userUid);
        if (request == null) {
            throw new BusinessException(MemoErrorCode.INVALID_MEMO_REQUEST);
        }
        String text = blankToNull(request.resolvedText());
        List<NoteAttachment> attachments = toAttachments(userUid, request.imageUris(), request.imageKeys(), request.videoUris(), request.videoKeys(), request.files());
        validateMemo(text, attachments);

        Memo memo = Memo.builder()
                .uid(UUID.randomUUID())
                .userUid(userUid)
                .text(text)
                .bookmarked(false)
                .build();

        memo.replaceAttachments(attachments);
        return MemoDto.from(memoRepository.saveAndFlush(memo), this::resolveAttachmentUrl);
    }

    @Override
    @Transactional
    public MemoDto createImageMemo(String content, MultipartFile file) {
        return createImageMemo(content, List.of(file));
    }

    @Override
    @Transactional
    public MemoDto createImageMemo(String content, List<MultipartFile> files) {
        ImageUploadResponse uploadResponse = uploadService.uploadImages(files);
        List<String> keys = uploadResponse.keys();
        try {
            return createMemo(new NewMemoDto(
                    null,
                    null,
                    content,
                    null,
                    keys,
                    null,
                    null,
                    null
            ));
        } catch (RuntimeException e) {
            deleteUploadedKeys(keys);
            throw e;
        }
    }

    @Override
    @Transactional
    public MemoDto createVideoMemo(String content, MultipartFile file) {
        VideoUploadResponse uploadResponse = uploadService.uploadVideo(file);
        List<String> keys = uploadResponse.key() == null ? List.of() : List.of(uploadResponse.key());
        try {
            return createMemo(new NewMemoDto(
                    null,
                    null,
                    content,
                    null,
                    null,
                    null,
                    keys,
                    null
            ));
        } catch (RuntimeException e) {
            deleteUploadedKeys(keys);
            throw e;
        }
    }

    @Override
    @Transactional
    public MemoDto createFileMemo(String content, MultipartFile file) {
        FileUploadResponse uploadResponse = uploadService.uploadFile(file);
        List<String> keys = uploadResponse.key() == null ? List.of() : List.of(uploadResponse.key());
        try {
            return createMemo(new NewMemoDto(
                    null,
                    null,
                    content,
                    null,
                    null,
                    null,
                    null,
                    List.of(new FileAttachmentDto(
                            uploadResponse.uid(),
                            uploadResponse.name(),
                            uploadResponse.url(),
                            uploadResponse.key(),
                            uploadResponse.mimeType(),
                            uploadResponse.size(),
                            null,
                            null
                    ))
            ));
        } catch (RuntimeException e) {
            deleteUploadedKeys(keys);
            throw e;
        }
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
        return MemoDto.from(memo, this::resolveAttachmentUrl);
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
        return BoardDto.from(savedBoard, this::resolveAttachmentUrl);
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
        return BoardDto.from(board, this::resolveAttachmentUrl);
    }

    private Memo getCurrentUserMemo(UUID memoUid, UUID userUid) {
        return memoRepository.findByUidAndUserUid(memoUid, userUid)
                .orElseThrow(() -> new BusinessException(MemoErrorCode.MEMO_NOT_FOUND));
    }

    private Note createNoteFromMemo(Memo memo, String noteTitle, String content) {
        String title = blankToNull(noteTitle);
        if (title == null) {
            title = blankToNull(memo.getText());
        }
        if (title == null) {
            title = memo.getAttachments().stream()
                    .map(NoteAttachment::getOriginalName)
                    .filter(name -> name != null && !name.isBlank())
                    .findFirst()
                    .orElse("첨부 메모");
        }
        if (title.length() > 100) {
            title = title.substring(0, 100);
        }
        Note note = Note.builder()
                .uid(UUID.randomUUID())
                .title(title)
                .content(blankToNull(content))
                .build();
        note.replaceAttachments(copyAttachments(memo.getAttachments()));
        return note;
    }

    private List<NoteAttachment> toAttachments(
            UUID userUid,
            List<String> imageUris,
            List<String> imageKeys,
            List<String> videoUris,
            List<String> videoKeys,
            List<FileAttachmentDto> files
    ) {
        ArrayList<NoteAttachment> attachments = new ArrayList<>();

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
            throw new BusinessException(MemoErrorCode.INVALID_MEMO_REQUEST);
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
                .map(String::trim)
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

    private List<NoteAttachment> copyAttachments(List<NoteAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .map(attachment -> NoteAttachment.builder()
                        .uid(UUID.randomUUID())
                        .userUid(attachment.getUserUid())
                        .type(attachment.getType())
                        .originalName(attachment.getOriginalName())
                        .storedName(attachment.getStoredName())
                        .url(attachment.getUrl())
                        .bucket(attachment.getBucket())
                        .s3Key(attachment.getS3Key())
                        .mimeType(attachment.getMimeType())
                        .sizeBytes(attachment.getSizeBytes())
                        .thumbnailUrl(attachment.getThumbnailUrl())
                        .durationSeconds(attachment.getDurationSeconds())
                        .status(attachment.getStatus())
                        .build())
                .toList();
    }

    private void validateUserExists(UUID userUid) {
        if (!userReader.existsByUid(userUid)) {
            throw new BusinessException(MemoErrorCode.USER_UID_NOT_FOUND);
        }
    }

    private void validateMemo(String text, List<NoteAttachment> attachments) {
        if ((text == null || text.isBlank()) && (attachments == null || attachments.isEmpty())) {
            throw new BusinessException(MemoErrorCode.INVALID_MEMO_REQUEST);
        }
        if (text != null && text.length() > 2000) {
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

    private void deleteUploadedKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        keys.stream()
                .filter(key -> key != null && !key.isBlank())
                .forEach(key -> {
                    try {
                        uploadService.deleteObject(key);
                    } catch (RuntimeException ignored) {
                    }
                });
    }

    private String resolveAttachmentUrl(NoteAttachment attachment) {
        String key = resolveAttachmentKey(attachment);
        if (key == null) {
            return attachment.getUrl();
        }
        return uploadService.createReadUrl(key);
    }

    private String resolveAttachmentKey(NoteAttachment attachment) {
        if (attachment.getS3Key() != null && !attachment.getS3Key().isBlank()) {
            return attachment.getS3Key().trim();
        }
        String url = attachment.getUrl();
        if (url == null || url.isBlank()) {
            return null;
        }
        String normalized = url.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("/")) {
            return null;
        }
        return normalized.contains("/users/")
                && (normalized.contains("/images/") || normalized.contains("/videos/") || normalized.contains("/files/"))
                ? normalized
                : null;
    }
}
