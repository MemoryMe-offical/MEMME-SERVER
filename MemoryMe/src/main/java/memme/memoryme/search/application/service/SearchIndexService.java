package memme.memoryme.search.application.service;

import memme.memoryme.search.domain.SearchDocument;

import java.util.List;
import java.util.UUID;

public interface SearchIndexService {
    void reindexUser(UUID userUid, List<SearchDocument> documents);
    void deleteUserIndex(UUID userUid);
}
