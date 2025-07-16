package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_history_id", nullable = false)
    private Long diagnosisHistoryId;

    @ManyToOne
    @JoinColumn(name = "image_analysis_id", nullable = false)
    private ImageAnalysis imageAnalysis;

    @Lob
    @Column(name = "diagnosis_text", columnDefinition = "TEXT", nullable = false)
    private String diagnosisText;

    @ManyToOne
    @JoinColumn(name = "changed_by_doctor_id")
    private Doctor changedByDoctor;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "change_datetime")
    private LocalDateTime changeDatetime;

    @Lob
    @Column(name = "analysis_details", columnDefinition = "TEXT")
    private String analysisDetails;

    @Lob
    @Column(name = "treatment_recommendations", columnDefinition = "TEXT")
    private String treatmentRecommendations;

    @PrePersist
    public void onCreate() {
        this.changeDatetime = LocalDateTime.now();
    }
}