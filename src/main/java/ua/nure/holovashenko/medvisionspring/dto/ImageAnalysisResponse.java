package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;

import java.time.LocalDateTime;

@Data
public class ImageAnalysisResponse {
    private Long imageAnalysisId;
    private Float analysisAccuracy;
    private Float analysisPrecision;
    private Float analysisRecall;
    private String analysisDetails;
    private String analysisDiagnosis;
    private String treatmentRecommendations;
    private LocalDateTime creationDatetime;
    private AnalysisStatus analysisStatus;
    private Integer diagnosisClass;

    private Long imageFileId;
    private Long heatmapFileId;
    private Long patientId;
    private Long doctorId;
    private Long hospitalId;
    private Long treatmentId;
    private Long analysisJobId;
    private Long modelVersionId;
    private String modelVersion;
}
