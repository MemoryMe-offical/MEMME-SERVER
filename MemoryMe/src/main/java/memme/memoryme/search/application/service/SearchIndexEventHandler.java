package memme.memoryme.search.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import memme.memoryme.search.config.SearchProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexEventHandler {
    private final SearchProperties searchProperties;
    private final SearchReindexService searchReindexService;

    @Transactional(readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void reindexAfterCommit(SearchReindexEvent event) {
        if (!searchProperties.isEnabled()) {
            return;
        }
        try {
            searchReindexService.reindexUser(event.userUid());
        } catch (RuntimeException e) {
            log.warn("Failed to reindex search documents. userUid={}", event.userUid(), e);
        }
    }
}
