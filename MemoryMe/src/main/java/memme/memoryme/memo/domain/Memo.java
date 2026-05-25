package memme.memoryme.memo.domain;

import jakarta.persistence.*;
import lombok.*;
import memme.memoryme.note.domain.NoteAttachment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Column(name = "title", length = 2000)
    private String text;

    @Column(nullable = false)
    private boolean bookmarked;

    @OneToMany(mappedBy = "memo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<NoteAttachment> attachments = new ArrayList<>();

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updatedAt;

    public void changeBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
        touch();
    }

    public void replaceAttachments(List<NoteAttachment> newAttachments) {
        Map<UUID, NoteAttachment> existingByUid = this.attachments.stream()
                .filter(attachment -> attachment.getUid() != null)
                .collect(Collectors.toMap(
                        NoteAttachment::getUid,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<NoteAttachment> replacements = new ArrayList<>();
        if (newAttachments != null) {
            for (NoteAttachment newAttachment : newAttachments) {
                NoteAttachment attachment = existingByUid.get(newAttachment.getUid());
                if (attachment != null) {
                    attachment.updateFrom(newAttachment);
                } else {
                    attachment = newAttachment;
                }
                attachment.assignMemo(this);
                replacements.add(attachment);
            }
        }

        this.attachments.removeIf(existing -> replacements.stream().noneMatch(replacement -> replacement == existing));
        replacements.stream()
                .filter(replacement -> !this.attachments.contains(replacement))
                .forEach(this.attachments::add);
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
