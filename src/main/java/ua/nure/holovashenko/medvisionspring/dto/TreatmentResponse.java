package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class TreatmentResponse {
    private Long treatmentId;
    private Long hospitalId;
    private String hospitalName;
    private Long patientId;
    private String patientName;
    private Long primaryDoctorId;
    private String primaryDoctorName;
    private String title;
    private String description;
    private TreatmentStatus status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String closeReason;
    private int analysisCount;
}
