package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    @Column(name = "analysis_diagnosis", columnDefinition = "TEXT")
    private String analysisDiagnosis;

    @Column(name = "creation_datetime")
    private LocalDateTime creationDatetime;

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