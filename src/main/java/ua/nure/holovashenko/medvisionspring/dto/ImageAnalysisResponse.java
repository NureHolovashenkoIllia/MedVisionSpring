package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageAnalysisResponse {
    private Long imageAnalysisId;
    private Float analysisAccuracy;
    private Float analysisPrecision;
    private Float analysisRecall;
    private String analysisDiagnosis;
    private LocalDateTime creationDatetime;

    private Long imageFileId;
    private Long heatmapFileId;
    private Long patientId;
    private Long doctorId;
}
