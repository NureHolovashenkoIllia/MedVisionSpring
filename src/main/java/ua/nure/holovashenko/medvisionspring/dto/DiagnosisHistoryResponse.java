package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DiagnosisHistoryResponse {
    private Long id;
    private String diagnosisText;
    private String doctorName;
    private String reason;
    private LocalDateTime timestamp;
    private String analysisDetails;
    private String treatmentRecommendations;
}
