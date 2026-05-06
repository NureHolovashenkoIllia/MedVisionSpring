package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TreatmentConclusionResponse {
    private Long conclusionId;
    private Long hospitalId;
    private Long treatmentId;
    private String conclusionText;
    private String recommendations;
    private Long createdByUserId;
    private String createdByUserName;
    private Long updatedByUserId;
    private String updatedByUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
