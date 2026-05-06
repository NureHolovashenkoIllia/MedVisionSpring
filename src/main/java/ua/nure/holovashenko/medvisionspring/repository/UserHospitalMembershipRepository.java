package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.entity.UserHospitalMembership;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHospitalMembershipRepository extends JpaRepository<UserHospitalMembership, Long> {
    List<UserHospitalMembership> findAllByUserAndStatus(User user, MembershipStatus status);

    List<UserHospitalMembership> findAllByHospital(Hospital hospital);

    List<UserHospitalMembership> findAllByHospitalAndHospitalRole(Hospital hospital, HospitalRole hospitalRole);

    Optional<UserHospitalMembership> findByUserAndHospitalAndHospitalRole(User user, Hospital hospital, HospitalRole hospitalRole);

    boolean existsByUserAndHospitalAndStatus(User user, Hospital hospital, MembershipStatus status);

    boolean existsByUserAndHospitalAndHospitalRoleAndStatus(
            User user,
            Hospital hospital,
            HospitalRole hospitalRole,
            MembershipStatus status
    );
}
