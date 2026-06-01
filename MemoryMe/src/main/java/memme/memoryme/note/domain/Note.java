package memme.memoryme.note.domain;

import jakarta.persistence.*;
import lombok.*;
import memme.memoryme.board.domain.Board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "note", indexes = {
        @Index(name = "idx_note_board_id", columnList = "board_id"),
        @Index(name = "idx_note_uid", columnList = "uid")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 2048)
    private String url;

    @ElementCollection
    @CollectionTable(name = "note_url", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "url", nullable = false, length = 2048)
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<String> urls = new ArrayList<>();

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

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<NoteAttachment> attachments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void assignBoard(Board board) {
        this.board = board;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void applyCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            return;
        }
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void update(String title, String content, List<String> urls, String ogTitle, String ogDescription, String ogImageUrl, String ogSiteName, String ogSummary) {
        this.title = title;
        this.content = content;
        replaceUrls(urls);
        this.ogTitle = ogTitle;
        this.ogDescription = ogDescription;
        this.ogImageUrl = ogImageUrl;
        this.ogSiteName = ogSiteName;
        this.ogSummary = ogSummary;
        touch();
    }

    public void replaceUrls(List<String> urls) {
        this.urls.clear();
        if (urls != null) {
            this.urls.addAll(urls);
        }
        this.url = this.urls.isEmpty() ? null : this.urls.get(0);
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
                attachment.assignNote(this);
                replacements.add(attachment);
            }
        }

        this.attachments.removeIf(existing -> replacements.stream().noneMatch(replacement -> replacement == existing));
        replacements.stream()
                .filter(replacement -> !this.attachments.contains(replacement))
                .forEach(this.attachments::add);
        touch();
    }

    public void addAttachment(NoteAttachment attachment) {
        attachment.assignNote(this);
        this.attachments.add(attachment);
    }

    public void removeAttachment(NoteAttachment attachment) {
        this.attachments.remove(attachment);
        attachment.assignNote(null);
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
