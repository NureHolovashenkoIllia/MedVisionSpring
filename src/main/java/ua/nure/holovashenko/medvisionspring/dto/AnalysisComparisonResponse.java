package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisComparisonResponse {
    private Long comparisonId;
    private Long hospitalId;
    private Long treatmentId;
    private Long fromAnalysisId;
    private Long toAnalysisId;
    private Integer diagnosisClassFrom;
    private Integer diagnosisClassTo;
    private boolean diagnosisChanged;
    private Float accuracyDelta;
    private Float precisionDelta;
    private Float recallDelta;
    private String doctorNotes;
    private Long createdByUserId;
    private String createdByUserName;
    private LocalDateTime createdAt;
}
