package memme.memoryme.note.infra.repository;

import memme.memoryme.note.domain.AttachmentStatus;
import memme.memoryme.note.domain.AttachmentType;
import memme.memoryme.note.domain.NoteAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteAttachmentRepository extends JpaRepository<NoteAttachment, Long> {
    Optional<NoteAttachment> findByUidAndUserUidAndStatus(UUID uid, UUID userUid, AttachmentStatus status);

    @Query(value = """
            select attachment
            from NoteAttachment attachment
            left join fetch attachment.note note
            left join fetch note.board
            left join fetch attachment.memo memo
            where attachment.userUid = :userUid
              and attachment.status = :status
              and (:type is null or attachment.type = :type)
            """,
            countQuery = """
            select count(attachment)
            from NoteAttachment attachment
            where attachment.userUid = :userUid
              and attachment.status = :status
              and (:type is null or attachment.type = :type)
            """)
    Page<NoteAttachment> findPage(
            @Param("userUid") UUID userUid,
            @Param("type") AttachmentType type,
            @Param("status") AttachmentStatus status,
            Pageable pageable
    );

    @Query("select a.s3Key from NoteAttachment a where a.userUid = :userUid and a.s3Key is not null")
    List<String> findAllS3KeysByUserUid(@Param("userUid") UUID userUid);
}
