package memme.memoryme.note.domain;

import jakarta.persistence.*;
import lombok.*;
import memme.memoryme.board.domain.Board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "og_title")
    private String ogTitle;

    @Column(name = "og_description", length = 1000)
    private String ogDescription;

    @Column(name = "og_image_url", length = 2048)
    private String ogImageUrl;

    @Column(name = "og_site_name")
    private String ogSiteName;

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

    public void update(String title, String content, String url, String ogTitle, String ogDescription, String ogImageUrl, String ogSiteName) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.ogTitle = ogTitle;
        this.ogDescription = ogDescription;
        this.ogImageUrl = ogImageUrl;
        this.ogSiteName = ogSiteName;
        touch();
    }

    public void replaceAttachments(List<NoteAttachment> newAttachments) {
        this.attachments.clear();
        if (newAttachments != null) {
            newAttachments.forEach(this::addAttachment);
        }
        touch();
    }

    public void addAttachment(NoteAttachment attachment) {
        attachment.assignNote(this);
        this.attachments.add(attachment);
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
