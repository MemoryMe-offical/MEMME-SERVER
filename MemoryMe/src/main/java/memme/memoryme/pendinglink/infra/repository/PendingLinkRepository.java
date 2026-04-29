package memme.memoryme.pendinglink.infra.repository;

import memme.memoryme.pendinglink.domain.PendingLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingLinkRepository extends JpaRepository<PendingLink, Long> {
    List<PendingLink> findAllByUserUidOrderByReceivedAtDesc(UUID userUid);
    Optional<PendingLink> findByUidAndUserUid(UUID uid, UUID userUid);
    boolean existsByUidAndUserUid(UUID uid, UUID userUid);
    void deleteByUidAndUserUid(UUID uid, UUID userUid);
    long countByUserUid(UUID userUid);
}
