package memme.memoryme.search.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.domain.Board;
import memme.memoryme.board.infra.repository.BoardRepository;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.memo.domain.Memo;
import memme.memoryme.memo.infra.repository.MemoRepository;
import memme.memoryme.search.api.dto.SearchReindexResponse;
import memme.memoryme.search.config.SearchProperties;
import memme.memoryme.search.domain.SearchDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchReindexService {
    private final CurrentUserProvider currentUserProvider;
    private final MemoRepository memoRepository;
    private final BoardRepository boardRepository;
    private final SearchDocumentMapper documentMapper;
    private final SearchIndexService searchIndexService;
    private final SearchProperties searchProperties;

    @Transactional(readOnly = true)
    public SearchReindexResponse reindexCurrentUser() {
        return reindexUser(currentUserProvider.getUid());
    }

    @Transactional(readOnly = true)
    public SearchReindexResponse reindexUser(UUID userUid) {
        List<Memo> memos = memoRepository.findAllByUserUid(userUid);
        List<Board> boards = boardRepository.findAllByUserUid(userUid);

        List<SearchDocument> documents = new ArrayList<>();
        memos.stream()
                .map(documentMapper::fromMemo)
                .forEach(documents::add);
        boards.stream()
                .map(documentMapper::fromBoard)
                .flatMap(List::stream)
                .forEach(documents::add);

        searchIndexService.reindexUser(userUid, documents);

        int boardCount = boards.size();
        int memoCount = memos.size();
        int noteCount = documents.size() - boardCount - memoCount;
        return new SearchReindexResponse(
                searchProperties.getIndexName(),
                userUid,
                documents.size(),
                memoCount,
                boardCount,
                noteCount
        );
    }
}
