package memme.memoryme.search.application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.search.api.dto.SearchItemDto;
import memme.memoryme.search.config.SearchProperties;
import memme.memoryme.search.domain.SearchDocument;
import memme.memoryme.search.exception.SearchErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "true")
public class ElasticsearchSearchQueryService implements SearchQueryService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ElasticsearchClient elasticsearchClient;
    private final CurrentUserProvider currentUserProvider;
    private final SearchProperties searchProperties;

    @Override
    public memme.memoryme.search.api.dto.SearchResponse search(String q, String type, String cursor, Integer limit) {
        String keyword = normalizeKeyword(q);
        String normalizedType = normalizeType(type);
        int normalizedLimit = normalizeLimit(limit);
        SearchCursor decodedCursor = decodeCursor(cursor);
        UUID userUid = currentUserProvider.getUid();

        try {
            SearchResponse<SearchDocument> response = elasticsearchClient.search(
                    buildRequest(userUid, keyword, normalizedType, decodedCursor, normalizedLimit),
                    SearchDocument.class
            );
            List<Hit<SearchDocument>> hits = response.hits().hits();
            boolean hasNext = hits.size() > normalizedLimit;
            List<Hit<SearchDocument>> visibleHits = hasNext ? hits.subList(0, normalizedLimit) : hits;
            String nextCursor = hasNext && !visibleHits.isEmpty()
                    ? encodeCursor(visibleHits.get(visibleHits.size() - 1))
                    : null;

            return new memme.memoryme.search.api.dto.SearchResponse(
                    visibleHits.stream().map(this::toItem).toList(),
                    hasNext,
                    nextCursor,
                    normalizedLimit,
                    response.hits().total() == null ? visibleHits.size() : response.hits().total().value()
            );
        } catch (ElasticsearchException | IOException e) {
            throw new BusinessException(SearchErrorCode.SEARCH_QUERY_FAILED);
        }
    }

    private SearchRequest buildRequest(UUID userUid, String keyword, String type, SearchCursor cursor, int limit) {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(searchProperties.getIndexName())
                .query(buildQuery(userUid, keyword, type))
                .size(limit + 1)
                .trackTotalHits(track -> track.enabled(true))
                .sort(sort -> sort.score(score -> score.order(SortOrder.Desc)))
                .sort(sort -> sort.field(field -> field
                        .field("updatedAt")
                        .order(SortOrder.Desc)
                        .format("strict_date_optional_time")
                ))
                .sort(sort -> sort.field(field -> field
                        .field("uid")
                        .order(SortOrder.Asc)
                ));

        if (cursor != null) {
            builder.searchAfter(List.of(
                    FieldValue.of(cursor.score()),
                    FieldValue.of(cursor.updatedAt()),
                    FieldValue.of(cursor.uid())
            ));
        }
        return builder.build();
    }

    private Query buildQuery(UUID userUid, String keyword, String type) {
        return Query.of(query -> query.bool(bool -> {
            bool.filter(filter -> filter.term(term -> term
                    .field("userUid")
                    .value(userUid.toString())
            ));
            if (type != null) {
                bool.filter(filter -> filter.term(term -> term
                        .field("type")
                        .value(type)
                ));
            }

            bool.minimumShouldMatch("1");
            bool.should(should -> should.multiMatch(multiMatch -> multiMatch
                    .query(keyword)
                    .type(TextQueryType.Phrase)
                    .fields(List.of(
                            "title^8",
                            "content^5",
                            "parentTitle^3",
                            "ogTitle^4",
                            "ogDescription^2",
                            "ogSummary^3",
                            "attachmentNames^5"
                    ))
                    .boost(6.0f)
            ));
            bool.should(should -> should.multiMatch(multiMatch -> multiMatch
                    .query(keyword)
                    .fields(List.of(
                            "title^5",
                            "content^3",
                            "tags^4",
                            "parentTitle^2",
                            "ogTitle^3",
                            "ogDescription",
                            "ogSummary^2",
                            "attachmentNames^4",
                            "urls"
                    ))
                    .boost(3.0f)
            ));
            bool.should(should -> should.multiMatch(multiMatch -> multiMatch
                    .query(keyword)
                    .fields(List.of(
                            "title.ngram^2",
                            "content.ngram",
                            "parentTitle.ngram",
                            "ogTitle.ngram",
                            "ogDescription.ngram",
                            "ogSummary.ngram",
                            "attachmentNames.ngram^2"
                    ))
            ));
            bool.should(should -> should.multiMatch(multiMatch -> multiMatch
                    .query(keyword)
                    .fields(List.of(
                            "title.keyword^10",
                            "parentTitle.keyword^5",
                            "ogTitle.keyword^5",
                            "attachmentNames.keyword^7",
                            "tags.keyword^6",
                            "urls^3"
                    ))
                    .boost(8.0f)
            ));
            return bool;
        }));
    }

    private SearchItemDto toItem(Hit<SearchDocument> hit) {
        SearchDocument document = hit.source();
        if (document == null) {
            throw new BusinessException(SearchErrorCode.SEARCH_QUERY_FAILED);
        }
        return new SearchItemDto(
                document.type(),
                document.uid(),
                displayTitle(document),
                document.content(),
                snippet(document),
                document.parentType(),
                document.parentUid(),
                document.parentTitle(),
                document.tags(),
                document.urls(),
                document.attachmentNames(),
                document.bookmarked(),
                document.createdAt(),
                document.updatedAt(),
                hit.score()
        );
    }

    private String displayTitle(SearchDocument document) {
        if (document.title() != null && !document.title().isBlank()) {
            return document.title();
        }
        if (SearchDocument.TYPE_MEMO.equals(document.type())) {
            return abbreviate(document.content(), 40);
        }
        return document.parentTitle();
    }

    private String snippet(SearchDocument document) {
        List<String> candidates = new ArrayList<>();
        candidates.add(document.content());
        candidates.add(document.ogSummary());
        candidates.add(document.ogDescription());
        candidates.add(document.parentTitle());
        candidates.addAll(document.attachmentNames());
        return candidates.stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .map(value -> abbreviate(value, 140))
                .orElse(null);
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private String normalizeKeyword(String q) {
        if (q == null || q.isBlank()) {
            throw new BusinessException(SearchErrorCode.INVALID_SEARCH_REQUEST);
        }
        String keyword = q.trim();
        if (keyword.length() > 100) {
            throw new BusinessException(SearchErrorCode.INVALID_SEARCH_REQUEST);
        }
        return keyword;
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        if (!SearchDocument.TYPE_MEMO.equals(normalized)
                && !SearchDocument.TYPE_BOARD.equals(normalized)
                && !SearchDocument.TYPE_NOTE.equals(normalized)) {
            throw new BusinessException(SearchErrorCode.INVALID_SEARCH_REQUEST);
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String encodeCursor(Hit<SearchDocument> hit) {
        SearchDocument document = hit.source();
        if (document == null || document.updatedAt() == null || document.uid() == null) {
            return null;
        }
        String payload = (hit.score() == null ? 0.0 : hit.score())
                + "|" + document.updatedAt()
                + "|" + document.uid();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private SearchCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid cursor");
            }
            return new SearchCursor(Double.parseDouble(parts[0]), parts[1], parts[2]);
        } catch (RuntimeException e) {
            throw new BusinessException(SearchErrorCode.INVALID_SEARCH_REQUEST);
        }
    }

    private record SearchCursor(double score, String updatedAt, String uid) {
    }
}
