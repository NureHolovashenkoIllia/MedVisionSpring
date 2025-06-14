package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "image_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_analysis_id", nullable = false)
    private Long imageAnalysisId;

    @Column(name = "analysis_accuracy")
    private Float analysisAccuracy;

    @Column(name = "analysis_precision")
    private Float analysisPrecision;

    @Column(name = "analysis_recall")
    private Float analysisRecall;

    @Lob
    @Column(name = "analysis_details", columnDefinition = "TEXT")
    private String analysisDetails;

    @Lob
    @Column(name = "analysis_diagnosis", columnDefinition = "TEXT")
    private String analysisDiagnosis;

    @Lob
    @Column(name = "treatment_recommendations", columnDefinition = "TEXT")
    private String treatmentRecommendations;

    @Column(name = "creation_datetime")
    private LocalDateTime creationDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status")
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(nullable = false, name = "viewed")
    private boolean viewed = false;

    @Column(name = "diagnosis_class")
    private Integer diagnosisClass;

    @ManyToOne
    @JoinColumn(name = "image_file_id")
    private ImageFile imageFile;

    @ManyToOne
    @JoinColumn(name = "heatmap_file_id")
    private ImageFile heatmapFile;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;
}