package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 100)
    @Column(name = "user_name", length = 100)
    private String userName;

    @Size(max = 100)
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @Column(name = "pw", nullable = false, length = 255)
    private String pw;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Column(name = "creation_datetime", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creationDatetime = LocalDateTime.now();
}
