package memme.memoryme.note.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "note_attachment", indexes = {
        @Index(name = "idx_attachment_note_id", columnList = "note_id"),
        @Index(name = "idx_attachment_user_uid", columnList = "user_uid")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NoteAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @Column(name = "user_uid")
    private UUID userUid;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private AttachmentType type;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "stored_name")
    private String storedName;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "bucket")
    private String bucket;

    @Column(name = "s3_key", length = 512)
    private String s3Key;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    @Builder.Default
    private AttachmentStatus status = AttachmentStatus.ATTACHED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void assignNote(Note note) {
        this.note = note;
    }

    public void updateFrom(NoteAttachment source) {
        this.userUid = source.userUid;
        this.type = source.type;
        this.originalName = source.originalName;
        this.storedName = source.storedName;
        this.url = source.url;
        this.bucket = source.bucket;
        this.s3Key = source.s3Key;
        this.mimeType = source.mimeType;
        this.sizeBytes = source.sizeBytes;
        this.thumbnailUrl = source.thumbnailUrl;
        this.durationSeconds = source.durationSeconds;
        this.status = source.status == null ? AttachmentStatus.ATTACHED : source.status;
    }

    public void markDeleted() {
        this.status = AttachmentStatus.DELETED;
    }

    @PrePersist
    void onCreate() {
        if (uid == null) {
            uid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AttachmentStatus.ATTACHED;
        }
    }
}
