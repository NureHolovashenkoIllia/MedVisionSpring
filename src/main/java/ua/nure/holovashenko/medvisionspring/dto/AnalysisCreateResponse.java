package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;

@Data
@Builder
public class AnalysisCreateResponse {
    private Long analysisId;
    private Long jobId;
    private Long treatmentId;
    private Long hospitalId;
    private AnalysisStatus status;
    private Integer diagnosisClass;
}
