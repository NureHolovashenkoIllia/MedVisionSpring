package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TreatmentCloseRequest {
    @NotBlank(message = "Причина закриття лікування не може бути порожньою")
    private String closeReason;

    private String doctorConclusion;
}
