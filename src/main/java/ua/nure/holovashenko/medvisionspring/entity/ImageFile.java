package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Size(max = 255)
    @Column(name = "image_file_url", length = 255, nullable = false)
    private String imageFileUrl;

    @Size(max = 255)
    @Column(name = "image_file_name", length = 255, nullable = false)
    private String imageFileName;

    @Size(max = 100)
    @Column(name = "image_file_type", length = 100, nullable = false)
    private String imageFileType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;
}