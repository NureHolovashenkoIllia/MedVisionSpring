package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnalysisNoteResponse {

    private Long analysisNoteId;
    private String noteText;

    private Integer noteAreaX;
    private Integer noteAreaY;
    private Integer noteAreaWidth;
    private Integer noteAreaHeight;

    private LocalDateTime creationDatetime;

    private Long imageAnalysisId;
    private Long imageFileId;
    private Long heatmapFileId;
    private Long doctorId;
}
