package memme.memoryme.upload.application.service;

import java.util.List;
import java.util.UUID;

public interface PendingS3DeleteService {
    void registerKeys(UUID userUid, List<String> s3Keys);
    void processUserKeys(UUID userUid);
}
