package memme.memoryme.search.application.service;

import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.search.domain.SearchDocument;
import memme.memoryme.search.exception.SearchErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopSearchIndexService implements SearchIndexService {
    @Override
    public void reindexUser(UUID userUid, List<SearchDocument> documents) {
        throw new BusinessException(SearchErrorCode.SEARCH_DISABLED);
    }

    @Override
    public void deleteUserIndex(UUID userUid) {
        // search disabled — no-op
    }
}
