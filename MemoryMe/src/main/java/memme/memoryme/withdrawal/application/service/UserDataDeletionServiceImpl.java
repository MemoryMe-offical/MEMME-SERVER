package memme.memoryme.withdrawal.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.board.application.service.BoardService;
import memme.memoryme.memo.application.service.MemoService;
import memme.memoryme.note.infra.repository.NoteAttachmentRepository;
import memme.memoryme.pendinglink.application.service.PendingLinkService;
import memme.memoryme.search.application.service.SearchIndexService;
import memme.memoryme.upload.application.service.PendingS3DeleteService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDataDeletionServiceImpl implements UserDataDeletionService {

    private final PendingLinkService pendingLinkService;
    private final MemoService memoService;
    private final BoardService boardService;
    private final SearchIndexService searchIndexService;
    private final NoteAttachmentRepository noteAttachmentRepository;
    private final PendingS3DeleteService pendingS3DeleteService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void deleteUserData(UUID userUid) {
        // S3 키를 DB 삭제 전에 수집해서 pending_s3_delete에 등록 (같은 트랜잭션)
        List<String> s3Keys = noteAttachmentRepository.findAllS3KeysByUserUid(userUid);
        pendingS3DeleteService.registerKeys(userUid, s3Keys);

        pendingLinkService.deleteAllByUserUid(userUid);
        memoService.deleteAllByUserUid(userUid);
        boardService.deleteAllByUserUid(userUid);
        searchIndexService.deleteUserIndex(userUid);

        eventPublisher.publishEvent(new UserDataDeletedEvent(userUid));
    }
}
