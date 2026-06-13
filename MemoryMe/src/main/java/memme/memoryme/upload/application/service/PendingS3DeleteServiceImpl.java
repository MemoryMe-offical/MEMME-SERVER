package memme.memoryme.upload.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import memme.memoryme.upload.domain.PendingS3Delete;
import memme.memoryme.upload.infra.PendingS3DeleteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingS3DeleteServiceImpl implements PendingS3DeleteService {

    private final PendingS3DeleteRepository pendingS3DeleteRepository;
    private final UploadService uploadService;

    @Override
    @Transactional
    public void registerKeys(UUID userUid, List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return;
        }
        List<PendingS3Delete> entries = s3Keys.stream()
                .filter(key -> key != null && !key.isBlank())
                .map(key -> PendingS3Delete.builder()
                        .userUid(userUid)
                        .s3Key(key.trim())
                        .build())
                .toList();
        pendingS3DeleteRepository.saveAll(entries);
    }

    @Override
    @Transactional
    public void processUserKeys(UUID userUid) {
        List<PendingS3Delete> pending = pendingS3DeleteRepository.findAllByUserUid(userUid);
        deleteS3Keys(pending);
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void retryStale() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<PendingS3Delete> stale = pendingS3DeleteRepository.findAllByCreatedAtBefore(threshold);
        if (!stale.isEmpty()) {
            log.info("Retrying {} stale pending S3 deletes", stale.size());
            deleteS3Keys(stale);
        }
    }

    private void deleteS3Keys(List<PendingS3Delete> entries) {
        List<Long> successIds = entries.stream()
                .filter(entry -> tryDeleteS3(entry.getS3Key()))
                .map(PendingS3Delete::getId)
                .toList();
        if (!successIds.isEmpty()) {
            pendingS3DeleteRepository.deleteAllById(successIds);
        }
    }

    private boolean tryDeleteS3(String key) {
        try {
            uploadService.deleteObject(key);
            return true;
        } catch (Exception e) {
            log.warn("S3 delete failed for key={}, will retry later", key, e);
            return false;
        }
    }
}
