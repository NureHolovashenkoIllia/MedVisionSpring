package ua.nure.holovashenko.medvisionspring.service;

import org.junit.jupiter.api.Test;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.UserHospitalMembershipRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HospitalAccessPolicyTests {

    private final UserHospitalMembershipRepository membershipRepository = mock(UserHospitalMembershipRepository.class);
    private final HospitalAccessPolicy policy = new HospitalAccessPolicy(membershipRepository);

    @Test
    void globalAdminCanManageAnyHospital() {
        User admin = User.builder().userRole(UserRole.ADMIN).build();
        Hospital hospital = Hospital.builder().hospitalId(1L).build();

        assertThat(policy.canManageHospital(admin, hospital)).isTrue();
    }

    @Test
    void hospitalAdminMembershipCanManageHospital() {
        User user = User.builder().userId(10L).userRole(UserRole.DOCTOR).build();
        Hospital hospital = Hospital.builder().hospitalId(1L).build();

        when(membershipRepository.existsByUserAndHospitalAndHospitalRoleAndStatus(
                user,
                hospital,
                HospitalRole.HOSPITAL_ADMIN,
                MembershipStatus.ACTIVE
        )).thenReturn(true);

        assertThat(policy.canManageHospital(user, hospital)).isTrue();
    }

    @Test
    void userWithoutMembershipCannotAccessHospital() {
        User user = User.builder().userId(10L).userRole(UserRole.PATIENT).build();
        Hospital hospital = Hospital.builder().hospitalId(1L).build();

        assertThatThrownBy(() -> policy.requireHospitalAccess(user, hospital))
                .isInstanceOf(ApiException.class)
                .hasMessage("Немає доступу до цієї лікарні");
    }
}
