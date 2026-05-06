package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.UserHospitalMembershipRepository;

@Service
@RequiredArgsConstructor
public class HospitalAccessPolicy {

    private final UserHospitalMembershipRepository membershipRepository;

    public boolean isGlobalAdmin(User user) {
        return user.getUserRole() == UserRole.ADMIN;
    }

    public boolean hasActiveMembership(User user, Hospital hospital) {
        return membershipRepository.existsByUserAndHospitalAndStatus(user, hospital, MembershipStatus.ACTIVE);
    }

    public boolean hasActiveRole(User user, Hospital hospital, HospitalRole role) {
        return membershipRepository.existsByUserAndHospitalAndHospitalRoleAndStatus(
                user,
                hospital,
                role,
                MembershipStatus.ACTIVE
        );
    }

    public boolean canManageHospital(User user, Hospital hospital) {
        return isGlobalAdmin(user) || hasActiveRole(user, hospital, HospitalRole.HOSPITAL_ADMIN);
    }

    public void requireHospitalAccess(User user, Hospital hospital) {
        if (!isGlobalAdmin(user) && !hasActiveMembership(user, hospital)) {
            throw new ApiException("Немає доступу до цієї лікарні", HttpStatus.FORBIDDEN);
        }
    }

    public void requireHospitalManager(User user, Hospital hospital) {
        if (!canManageHospital(user, hospital)) {
            throw new ApiException("Немає прав на керування цією лікарнею", HttpStatus.FORBIDDEN);
        }
    }
}
