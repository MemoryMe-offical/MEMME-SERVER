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
import memme.memoryme.timeline.api.dto.TimelineResponse;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.timeline.exception.TimelineErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final MemoRepository memoRepository;
    private final BoardRepository boardRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(String type, String tags, String q, String sort, Integer page, Integer limit, UUID excludeId) {
        UUID userUid = currentUserProvider.getUid();
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedLimit = limit == null || limit < 1 ? 50 : Math.min(limit, 100);
        String normalizedType = normalize(type);
        String keyword = normalize(q);
        Set<String> tagSet = parseTags(tags);
        validateType(normalizedType);
        validateSort(sort);

        List<TimelineItem> items = new ArrayList<>();

        if (normalizedType == null || normalizedType.equals("memo")) {
            if (tagSet.isEmpty()) {
                memoRepository.findAllByUserUid(userUid).stream()
                        .filter(memo -> excludeId == null || !memo.getUid().equals(excludeId))
                        .filter(memo -> keyword == null || contains(memo.getText(), keyword))
                        .map(memo -> new TimelineItem(MemoDto.from(memo), "memo", memo.getCreatedAt(), memo.getCreatedAt()))
                        .forEach(items::add);
            }
        }

        if (normalizedType == null || normalizedType.equals("board")) {
            boardRepository.findAllByUserUid(userUid).stream()
                    .filter(board -> excludeId == null || !board.getUid().equals(excludeId))
                    .filter(board -> tagSet.isEmpty() || board.getTags().containsAll(tagSet))
                    .filter(board -> keyword == null || boardMatches(board, keyword))
                    .map(board -> new TimelineItem(BoardDto.from(board), "board", board.getCreatedAt(), board.getUpdatedAt()))
                    .forEach(items::add);
        }

        Comparator<TimelineItem> comparator = "updatedAt".equals(sort)
                ? Comparator.comparing(TimelineItem::updatedAt)
                : Comparator.comparing(TimelineItem::createdAt);
        items.sort(comparator.reversed());

        int fromIndex = Math.min((normalizedPage - 1) * normalizedLimit, items.size());
        int toIndex = Math.min(fromIndex + normalizedLimit, items.size());

        return new TimelineResponse(
                items.subList(fromIndex, toIndex).stream().map(TimelineItem::data).toList(),
                items.size(),
                normalizedPage,
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

    private record TimelineItem(Object data, String type, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }
}
