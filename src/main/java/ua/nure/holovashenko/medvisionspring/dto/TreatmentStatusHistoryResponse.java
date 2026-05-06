package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class TreatmentStatusHistoryResponse {
    private Long historyId;
    private TreatmentStatus fromStatus;
    private TreatmentStatus toStatus;
    private Long changedByUserId;
    private String changedByUserName;
    private String reason;
    private LocalDateTime changedAt;
}
