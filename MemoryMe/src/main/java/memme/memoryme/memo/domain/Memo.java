package memme.memoryme.memo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "memo", indexes = {
        @Index(name = "idx_user_uid", columnList = "user_uid")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    // todo: 서비스단에서의 검증 필요
    @Column(name = "user_uid", nullable = false)
    private UUID userUid;
    private String title;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "note_post_id")
    private Post post;

    @Column(nullable = false)
    private LocalDateTime created;
    @Column(nullable = false)
    private LocalDateTime updated;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (created == null) {
            created = now;
        }

        if (updated == null) {
            updated = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updated = LocalDateTime.now();
    }
}