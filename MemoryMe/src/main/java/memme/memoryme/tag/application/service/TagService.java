package memme.memoryme.tag.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.infra.repository.BoardRepository;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.tag.api.dto.TagDto;
import memme.memoryme.tag.api.dto.TagListResponse;
import memme.memoryme.tag.exception.TagErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final BoardRepository boardRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public TagListResponse getTags(String q) {
        String normalizedQuery = blankToNull(q);
        if (normalizedQuery != null && normalizedQuery.length() > 30) {
            throw new BusinessException(TagErrorCode.INVALID_TAG_REQUEST);
        }
        return new TagListResponse(
                boardRepository.findTagCounts(currentUserProvider.getUid(), normalizedQuery).stream()
                        .map(tag -> new TagDto(tag.getName(), tag.getCount()))
                        .toList()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase();
    }
}
