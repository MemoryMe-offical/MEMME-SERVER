package memme.memoryme.board.infra.repository;

import memme.memoryme.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByUidAndUserUid(UUID uid, UUID userUid);
    boolean existsByUidAndUserUid(UUID uid, UUID userUid);
    List<Board> findAllByUserUid(UUID userUid);

    @Query("""
            select tag as name, count(board) as count
            from Board board join board.tags tag
            where board.userUid = :userUid
              and (:q is null or lower(tag) like lower(concat(:q, '%')))
            group by tag
            order by count(board) desc, tag asc
            """)
    List<TagCountProjection> findTagCounts(@Param("userUid") UUID userUid, @Param("q") String q);

    interface TagCountProjection {
        String getName();
        long getCount();
    }
}
