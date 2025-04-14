package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PASSWORD_RESET")
public class PasswordResetEntity {

    @Id
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiry; // 토큰 만료 시간

    @Setter
    @Column(nullable = false)
    private boolean used;

}
