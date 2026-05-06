package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.HospitalMembershipCreateRequest;
import ua.nure.holovashenko.medvisionspring.dto.HospitalMembershipResponse;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.entity.UserHospitalMembership;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.HospitalRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserHospitalMembershipRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalMembershipService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final UserHospitalMembershipRepository membershipRepository;
    private final HospitalAccessPolicy accessPolicy;

    @Transactional
    public HospitalMembershipResponse addMembership(
            Long hospitalId,
            HospitalMembershipCreateRequest request,
            UserDetails currentUserDetails
    ) {
        User currentUser = getUserOrThrow(currentUserDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalManager(currentUser, hospital);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));

        membershipRepository.findByUserAndHospitalAndHospitalRole(targetUser, hospital, request.getHospitalRole())
                .ifPresent(existing -> {
                    throw new ApiException("Такий membership вже існує", HttpStatus.CONFLICT);
                });

        UserHospitalMembership membership = UserHospitalMembership.builder()
                .user(targetUser)
                .hospital(hospital)
                .hospitalRole(request.getHospitalRole())
                .status(MembershipStatus.ACTIVE)
                .build();

        return mapToResponse(membershipRepository.save(membership));
    }

    public List<HospitalMembershipResponse> getMemberships(
            Long hospitalId,
            HospitalRole role,
            UserDetails currentUserDetails
    ) {
        User currentUser = getUserOrThrow(currentUserDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalManager(currentUser, hospital);

        List<UserHospitalMembership> memberships = role == null
                ? membershipRepository.findAllByHospital(hospital)
                : membershipRepository.findAllByHospitalAndHospitalRole(hospital, role);

        return memberships.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public HospitalMembershipResponse updateMembershipStatus(
            Long hospitalId,
            Long membershipId,
            MembershipStatus status,
            UserDetails currentUserDetails
    ) {
        User currentUser = getUserOrThrow(currentUserDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalManager(currentUser, hospital);

        UserHospitalMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ApiException("Membership не знайдено", HttpStatus.NOT_FOUND));

        if (!membership.getHospital().getHospitalId().equals(hospitalId)) {
            throw new ApiException("Membership не належить цій лікарні", HttpStatus.BAD_REQUEST);
        }

        membership.setStatus(status);
        return mapToResponse(membershipRepository.save(membership));
    }

    private Hospital getHospitalOrThrow(Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ApiException("Лікарню не знайдено", HttpStatus.NOT_FOUND));
    }

    private User getUserOrThrow(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    private HospitalMembershipResponse mapToResponse(UserHospitalMembership membership) {
        User user = membership.getUser();
        Hospital hospital = membership.getHospital();
        return HospitalMembershipResponse.builder()
                .membershipId(membership.getMembershipId())
                .hospitalId(hospital.getHospitalId())
                .hospitalName(hospital.getName())
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .hospitalRole(membership.getHospitalRole())
                .status(membership.getStatus())
                .build();
    }
}
