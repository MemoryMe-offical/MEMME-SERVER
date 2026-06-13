package memme.memoryme.withdrawal.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.upload.application.service.PendingS3DeleteService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class UserDataDeletedEventListener {

    private final PendingS3DeleteService pendingS3DeleteService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDataDeleted(UserDataDeletedEvent event) {
        pendingS3DeleteService.processUserKeys(event.userUid());
    }
}
