package memme.memoryme.search.application.service;

import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.search.api.dto.SearchResponse;
import memme.memoryme.search.exception.SearchErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopSearchQueryService implements SearchQueryService {
    @Override
    public SearchResponse search(String q, String type, String cursor, Integer limit) {
        throw new BusinessException(SearchErrorCode.SEARCH_DISABLED);
    }
}
