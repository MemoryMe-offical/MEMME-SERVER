package memme.memoryme.memo.infra.repository;

import memme.memoryme.memo.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    Optional<Memo> findByUidAndUserUid(UUID uid, UUID userUid);
    boolean existsByUidAndUserUid(UUID uid, UUID userUid);
    void deleteByUidAndUserUid(UUID uid, UUID userUid);
    List<Memo> findAllByUserUid(UUID userUid);
}
