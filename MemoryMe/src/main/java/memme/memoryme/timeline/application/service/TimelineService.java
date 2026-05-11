package memme.memoryme.timeline.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.api.dto.BoardDto;
import memme.memoryme.board.domain.Board;
import memme.memoryme.board.infra.repository.BoardRepository;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.memo.api.dto.memo.MemoDto;
import memme.memoryme.memo.domain.Memo;
import memme.memoryme.memo.infra.repository.MemoRepository;
import memme.memoryme.note.domain.Note;
import memme.memoryme.note.domain.NoteAttachment;
import memme.memoryme.timeline.api.dto.TimelineResponse;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.timeline.exception.TimelineErrorCode;
import memme.memoryme.upload.application.service.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final MemoRepository memoRepository;
    private final BoardRepository boardRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UploadService uploadService;

    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(String type, String tags, String q, String sort, String cursor, Integer limit, UUID excludeId) {
        UUID userUid = currentUserProvider.getUid();
        int normalizedLimit = limit == null || limit < 1 ? 50 : Math.min(limit, 100);
        String normalizedType = normalize(type);
        String keyword = normalize(q);
        Set<String> tagSet = parseTags(tags);
        validateType(normalizedType);
        validateSort(sort);
        TimelineCursor decodedCursor = decodeCursor(cursor);

        List<TimelineItem> items = new ArrayList<>();

        if (normalizedType == null || normalizedType.equals("memo")) {
            if (tagSet.isEmpty()) {
                memoRepository.findAllByUserUid(userUid).stream()
                        .filter(memo -> excludeId == null || !memo.getUid().equals(excludeId))
                        .filter(memo -> keyword == null || contains(memo.getText(), keyword))
                        .map(memo -> new TimelineItem(MemoDto.from(memo), memo.getUid(), memo.getCreatedAt(), memo.getCreatedAt()))
                        .forEach(items::add);
            }
        }

        if (normalizedType == null || normalizedType.equals("board")) {
            boardRepository.findAllByUserUid(userUid).stream()
                    .filter(board -> excludeId == null || !board.getUid().equals(excludeId))
                    .filter(board -> tagSet.isEmpty() || board.getTags().containsAll(tagSet))
                    .filter(board -> keyword == null || boardMatches(board, keyword))
                    .map(board -> new TimelineItem(BoardDto.from(board, this::resolveAttachmentUrl), board.getUid(), board.getCreatedAt(), board.getUpdatedAt()))
                    .forEach(items::add);
        }

        Comparator<TimelineItem> comparator = "updatedAt".equals(sort)
                ? Comparator.comparing(TimelineItem::updatedAt)
                : Comparator.comparing(TimelineItem::createdAt);
        items.sort(comparator.reversed());

        int fromIndex = cursorStartIndex(items, decodedCursor, sort);
        int toIndex = Math.min(fromIndex + normalizedLimit + 1, items.size());
        List<TimelineItem> slice = items.subList(fromIndex, toIndex);
        boolean hasNext = slice.size() > normalizedLimit;
        List<TimelineItem> visibleItems = hasNext ? slice.subList(0, normalizedLimit) : slice;
        String nextCursor = hasNext && !visibleItems.isEmpty()
                ? encodeCursor(visibleItems.get(visibleItems.size() - 1), sort)
                : null;

        return new TimelineResponse(
                visibleItems.stream().map(TimelineItem::data).toList(),
                hasNext,
                nextCursor,
                normalizedLimit
        );
    }

    private boolean boardMatches(Board board, String keyword) {
        if (contains(board.getTitle(), keyword) || contains(board.getDescription(), keyword)) {
            return true;
        }
        boolean tagMatched = board.getTags().stream().anyMatch(tag -> contains(tag, keyword));
        if (tagMatched) {
            return true;
        }
        return board.getNotes().stream().anyMatch(note -> noteMatches(note, keyword));
    }

    private boolean noteMatches(Note note, String keyword) {
        return contains(note.getTitle(), keyword)
                || contains(note.getContent(), keyword)
                || contains(note.getUrl(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Set<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> parsed = new LinkedHashSet<>();
        Arrays.stream(tags.split(","))
                .map(this::normalize)
                .filter(Objects::nonNull)
                .forEach(parsed::add);
        return parsed;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateType(String type) {
        if (type != null && !type.equals("memo") && !type.equals("board")) {
            throw new BusinessException(TimelineErrorCode.INVALID_TIMELINE_REQUEST);
        }
    }

    private void validateSort(String sort) {
        if (sort != null && !sort.equals("createdAt") && !sort.equals("updatedAt")) {
            throw new BusinessException(TimelineErrorCode.INVALID_TIMELINE_REQUEST);
        }
    }

    private int cursorStartIndex(List<TimelineItem> items, TimelineCursor cursor, String sort) {
        if (cursor == null) {
            return 0;
        }
        for (int i = 0; i < items.size(); i++) {
            TimelineItem item = items.get(i);
            if (item.uid().equals(cursor.uid())) {
                return i + 1;
            }
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).sortAt(sort).isBefore(cursor.sortAt())) {
                return i;
            }
        }
        return items.size();
    }

    private String encodeCursor(TimelineItem item, String sort) {
        String payload = item.sortAt(sort).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + item.uid();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private TimelineCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", 2);
            return new TimelineCursor(LocalDateTime.parse(parts[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME), UUID.fromString(parts[1]));
        } catch (RuntimeException e) {
            throw new BusinessException(TimelineErrorCode.INVALID_TIMELINE_REQUEST);
        }
    }

    private String resolveAttachmentUrl(NoteAttachment attachment) {
        if (attachment.getS3Key() == null || attachment.getS3Key().isBlank()) {
            return attachment.getUrl();
        }
        return uploadService.createReadUrl(attachment.getS3Key());
    }

    private record TimelineCursor(LocalDateTime sortAt, UUID uid) {
    }

    private record TimelineItem(Object data, UUID uid, LocalDateTime createdAt, LocalDateTime updatedAt) {
        private LocalDateTime sortAt(String sort) {
            return "updatedAt".equals(sort) ? updatedAt : createdAt;
        }
    }
}
