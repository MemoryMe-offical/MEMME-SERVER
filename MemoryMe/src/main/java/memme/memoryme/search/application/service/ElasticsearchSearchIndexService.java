package memme.memoryme.search.application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.search.config.SearchProperties;
import memme.memoryme.search.domain.SearchDocument;
import memme.memoryme.search.exception.SearchErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "true")
public class ElasticsearchSearchIndexService implements SearchIndexService {
    private final ElasticsearchClient elasticsearchClient;
    private final SearchProperties searchProperties;

    @Override
    public void reindexUser(UUID userUid, List<SearchDocument> documents) {
        try {
            deleteUserDocuments(userUid);
            bulkIndex(documents);
            refreshIndex();
        } catch (ElasticsearchException | IOException e) {
            throw new BusinessException(SearchErrorCode.SEARCH_INDEX_FAILED);
        }
    }

    private void deleteUserDocuments(UUID userUid) throws IOException {
        elasticsearchClient.deleteByQuery(request -> request
                .index(searchProperties.getIndexName())
                .query(query -> query.term(term -> term
                        .field("userUid")
                        .value(userUid.toString())
                ))
                .refresh(true)
        );
    }

    private void bulkIndex(List<SearchDocument> documents) throws IOException {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        documents.forEach(document -> bulkRequest.operations(operation -> operation
                .index(index -> index
                        .index(searchProperties.getIndexName())
                        .id(document.documentId())
                        .document(document)
                )
        ));

        BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
        if (response.errors()) {
            throw new BusinessException(SearchErrorCode.SEARCH_INDEX_FAILED);
        }
    }

    private void refreshIndex() throws IOException {
        elasticsearchClient.indices().refresh(request -> request.index(searchProperties.getIndexName()));
    }
}
