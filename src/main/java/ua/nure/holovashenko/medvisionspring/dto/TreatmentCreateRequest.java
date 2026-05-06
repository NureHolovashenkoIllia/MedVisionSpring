package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TreatmentCreateRequest {
    @NotNull
    private Long patientId;

    @NotBlank(message = "Назва лікування не може бути порожньою")
    @Size(max = 255)
    private String title;

    private String description;
}
