package memme.memoryme.note.infra.repository;

import memme.memoryme.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByUid(UUID uid);
    void deleteByUid(UUID uid);
}
