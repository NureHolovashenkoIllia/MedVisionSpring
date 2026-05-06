package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisJobStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisJobResponse {
    private Long jobId;
    private Long analysisId;
    private Long hospitalId;
    private Long treatmentId;
    private AnalysisJobStatus status;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
