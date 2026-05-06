package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.HospitalStatus;

@Data
public class HospitalUpdateRequest {
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String legalName;

    @Size(max = 64)
    private String code;

    @Size(max = 500)
    private String address;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String email;

    private HospitalStatus status;
}
