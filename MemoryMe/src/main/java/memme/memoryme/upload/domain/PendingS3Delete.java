package memme.memoryme.upload.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_s3_delete", indexes = {
        @Index(name = "idx_pending_s3_delete_user_uid", columnList = "user_uid"),
        @Index(name = "idx_pending_s3_delete_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PendingS3Delete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, updatable = false)
    private UUID userUid;

    @Column(name = "s3_key", nullable = false, length = 512, updatable = false)
    private String s3Key;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
