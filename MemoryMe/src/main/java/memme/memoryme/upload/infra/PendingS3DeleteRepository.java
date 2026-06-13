package memme.memoryme.upload.infra;

import memme.memoryme.upload.domain.PendingS3Delete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PendingS3DeleteRepository extends JpaRepository<PendingS3Delete, Long> {
    List<PendingS3Delete> findAllByUserUid(UUID userUid);
    List<PendingS3Delete> findAllByCreatedAtBefore(LocalDateTime threshold);
    void deleteAllByUserUid(UUID userUid);
}
