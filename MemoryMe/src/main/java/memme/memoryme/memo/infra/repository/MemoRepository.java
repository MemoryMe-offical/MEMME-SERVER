package memme.memoryme.memo.infra.repository;

import memme.memoryme.memo.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    Optional<Memo> findByUid(UUID uid);
    void deleteByUid(UUID uid);
}
