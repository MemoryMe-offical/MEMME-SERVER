package memme.memoryme.pendinglink.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_link", indexes = {
        @Index(name = "idx_pending_link_user_uid", columnList = "user_uid"),
        @Index(name = "idx_pending_link_received_at", columnList = "user_uid, received_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PendingLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(name = "user_uid", nullable = false, updatable = false)
    private UUID userUid;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "og_title")
    private String ogTitle;

    @Column(name = "og_description", length = 1000)
    private String ogDescription;

    @Column(name = "og_image_url", length = 2048)
    private String ogImageUrl;

    @Column(name = "og_site_name")
    private String ogSiteName;

    @Lob
    @Column(name = "og_summary", columnDefinition = "TEXT")
    private String ogSummary;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    void onCreate() {
        if (uid == null) {
            uid = UUID.randomUUID();
        }
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}
