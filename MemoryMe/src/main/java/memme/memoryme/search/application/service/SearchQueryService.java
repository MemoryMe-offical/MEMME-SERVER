package memme.memoryme.search.application.service;

import memme.memoryme.search.api.dto.SearchResponse;

public interface SearchQueryService {
    SearchResponse search(String q, String type, String cursor, Integer limit);
}
