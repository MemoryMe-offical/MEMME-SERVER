package memme.memoryme.memo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quick_memo", indexes = {
        @Index(name = "idx_memo_user_uid", columnList = "user_uid"),
        @Index(name = "idx_memo_user_created_at", columnList = "user_uid, created_at")
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

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private boolean bookmarked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void changeBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    @PrePersist
    void onCreate() {
        if (uid == null) {
            uid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
