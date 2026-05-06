package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisJobStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_job")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_job_id", nullable = false)
    private Long analysisJobId;

    @ManyToOne
    @JoinColumn(name = "image_analysis_id")
    private ImageAnalysis imageAnalysis;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AnalysisJobStatus status;

    @Lob
    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Lob
    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = AnalysisJobStatus.PENDING;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
