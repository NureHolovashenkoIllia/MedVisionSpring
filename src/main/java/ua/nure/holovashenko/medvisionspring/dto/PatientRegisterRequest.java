package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegisterRequest {

    @NotBlank(message = "Ім'я не може бути порожнім")
    private String name;

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    @Size(min = 8, message = "Пароль повинен містити щонайменше 8 символів")
    private String password;

    private LocalDate birthDate;
    private String gender;
}
