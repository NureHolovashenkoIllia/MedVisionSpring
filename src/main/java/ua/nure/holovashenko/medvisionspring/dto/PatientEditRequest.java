package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PatientEditRequest {

    @NotBlank(message = "Ім'я не може бути порожнім")
    private String name;

    @NotNull(message = "Дата народження є обов'язковою")
    private LocalDate birthDate;

    @NotBlank(message = "Стать є обов'язковою (MALE або FEMALE)")
    private String gender;
}