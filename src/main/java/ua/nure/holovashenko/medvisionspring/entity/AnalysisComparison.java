package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_comparison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisComparison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_comparison_id", nullable = false)
    private Long analysisComparisonId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(optional = false)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_analysis_id", nullable = false)
    private ImageAnalysis fromAnalysis;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_analysis_id", nullable = false)
    private ImageAnalysis toAnalysis;

    @Column(name = "diagnosis_class_from")
    private Integer diagnosisClassFrom;

    @Column(name = "diagnosis_class_to")
    private Integer diagnosisClassTo;

    @Column(name = "diagnosis_changed", nullable = false)
    private boolean diagnosisChanged;

    @Column(name = "accuracy_delta")
    private Float accuracyDelta;

    @Column(name = "precision_delta")
    private Float precisionDelta;

    @Column(name = "recall_delta")
    private Float recallDelta;

    @Lob
    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
