package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.HospitalStatus;

@Data
@Builder
public class HospitalResponse {
    private Long hospitalId;
    private String name;
    private String legalName;
    private String code;
    private String address;
    private String phone;
    private String email;
    private HospitalStatus status;
}
