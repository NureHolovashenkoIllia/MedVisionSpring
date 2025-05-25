package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;

@Data
public class DiagnosisHistoryRequest {
    private Long analysisId;
    private String diagnosisText;
    private Long doctorId; // опціонально
    private String reason; // опціонально
}
