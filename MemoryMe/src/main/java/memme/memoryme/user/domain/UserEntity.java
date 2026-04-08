package memme.memoryme.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
/*
todo: 무슨 컬럼을 기준으로 인덱스를 생성할건지 정하고 수정 필요
현재 user 컬럼이 없는데 user컬럼을 기준으로 인덱스를 생성하려 해서 하이버네이트 오류 발생중.
*/
@Table(name = "user"
        /*
        indexes = {
        @Index(name = "idx_user", columnList = "user")}
        */
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

}