package memme.memoryme.memo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "memo", indexes = {
        @Index(name = "idx_memo_user_uid", columnList = "user_uid"),
        @Index(name = "idx_memo_user_created", columnList = "user_uid, created")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(name = "user_uid", nullable = false, updatable = false)
    private UUID userUid;

    @Column(name = "title", nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private boolean bookmarked;

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updatedAt;

    public void changeBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
        touch();
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (uid == null) {
            uid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
