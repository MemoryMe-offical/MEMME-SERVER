package memme.memoryme.board.domain;

import jakarta.persistence.*;
import lombok.*;
import memme.memoryme.note.domain.Note;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "board", indexes = {
        @Index(name = "idx_board_user_uid", columnList = "user_uid"),
        @Index(name = "idx_board_user_created_at", columnList = "user_uid, created_at"),
        @Index(name = "idx_board_user_updated_at", columnList = "user_uid, updated_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(name = "user_uid", nullable = false, updatable = false)
    private UUID userUid;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "board_tag", joinColumns = @JoinColumn(name = "board_id"))
    @Column(name = "tag", nullable = false, length = 30)
    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean bookmarked;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC, createdAt ASC")
    @Builder.Default
    private List<Note> notes = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateMeta(String title, String description, Set<String> tags) {
        this.title = title;
        this.description = description;
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
        touch();
    }

    public void changeBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
        touch();
    }

    public void addNote(Note note) {
        note.assignBoard(this);
        note.changeSortOrder(nextSortOrder());
        this.notes.add(note);
        touch();
    }

    public void removeNote(Note note) {
        this.notes.remove(note);
        note.assignBoard(null);
        touch();
    }

    public int nextSortOrder() {
        return notes.stream()
                .mapToInt(Note::getSortOrder)
                .max()
                .orElse(0) + 1;
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
