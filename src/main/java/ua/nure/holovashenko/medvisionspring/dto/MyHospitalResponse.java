package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;

@Data
@Builder
public class MyHospitalResponse {
    private Long hospitalId;
    private String name;
    private String code;
    private HospitalRole hospitalRole;
    private MembershipStatus membershipStatus;
}
