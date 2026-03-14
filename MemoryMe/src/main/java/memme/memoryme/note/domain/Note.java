package memme.memoryme.note.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "note", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    // todo: 서비스단에서의 검증 필요
    @Column(name = "user_id", nullable = false)
    private Long userId;
    private String title;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "note_post_id")
    private NotePost notePost;

    @Column(nullable = false)
    private LocalDateTime created;
    @Column(nullable = false)
    private LocalDateTime updated;
}