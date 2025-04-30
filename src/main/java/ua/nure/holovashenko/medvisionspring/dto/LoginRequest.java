package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    private String password;
}
