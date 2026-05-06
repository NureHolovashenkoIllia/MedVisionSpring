package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;

@Data
@Builder
public class HospitalMembershipResponse {
    private Long membershipId;
    private Long hospitalId;
    private String hospitalName;
    private Long userId;
    private String userName;
    private String email;
    private HospitalRole hospitalRole;
    private MembershipStatus status;
}
