package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.entity.UserHospitalMembership;
import ua.nure.holovashenko.medvisionspring.enums.HospitalStatus;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.HospitalRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserHospitalMembershipRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final UserHospitalMembershipRepository membershipRepository;
    private final HospitalAccessPolicy accessPolicy;

    @Transactional
    public HospitalResponse createHospital(HospitalCreateRequest request) {
        if (request.getCode() != null && !request.getCode().isBlank() && hospitalRepository.existsByCode(request.getCode())) {
            throw new ApiException("Лікарня з таким кодом вже існує", HttpStatus.CONFLICT);
        }

        Hospital hospital = Hospital.builder()
                .name(request.getName())
                .legalName(request.getLegalName())
                .code(normalizeCode(request.getCode()))
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status(HospitalStatus.ACTIVE)
                .build();

        return mapToResponse(hospitalRepository.save(hospital));
    }

    public List<HospitalResponse> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public HospitalResponse updateHospital(Long hospitalId, HospitalUpdateRequest request) {
        Hospital hospital = getHospitalOrThrow(hospitalId);

        if (request.getCode() != null && !request.getCode().isBlank()) {
            hospitalRepository.findByCode(request.getCode())
                    .filter(existing -> !existing.getHospitalId().equals(hospitalId))
                    .ifPresent(existing -> {
                        throw new ApiException("Лікарня з таким кодом вже існує", HttpStatus.CONFLICT);
                    });
            hospital.setCode(normalizeCode(request.getCode()));
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            hospital.setName(request.getName());
        }
        if (request.getLegalName() != null) {
            hospital.setLegalName(request.getLegalName());
        }
        if (request.getAddress() != null) {
            hospital.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            hospital.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            hospital.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            hospital.setStatus(request.getStatus());
        }

        return mapToResponse(hospitalRepository.save(hospital));
    }

    public List<MyHospitalResponse> getMyHospitals(UserDetails userDetails) {
        User user = getUserOrThrow(userDetails);
        return membershipRepository.findAllByUserAndStatus(user, MembershipStatus.ACTIVE).stream()
                .map(this::mapToMyHospitalResponse)
                .toList();
    }

    public ActiveHospitalResponse selectHospital(Long hospitalId, UserDetails userDetails) {
        User user = getUserOrThrow(userDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalAccess(user, hospital);

        return ActiveHospitalResponse.builder()
                .hospitalId(hospital.getHospitalId())
                .name(hospital.getName())
                .selected(true)
                .build();
    }

    public Hospital getHospitalOrThrow(Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ApiException("Лікарню не знайдено", HttpStatus.NOT_FOUND));
    }

    private User getUserOrThrow(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    private HospitalResponse mapToResponse(Hospital hospital) {
        return HospitalResponse.builder()
                .hospitalId(hospital.getHospitalId())
                .name(hospital.getName())
                .legalName(hospital.getLegalName())
                .code(hospital.getCode())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .email(hospital.getEmail())
                .status(hospital.getStatus())
                .build();
    }

    private MyHospitalResponse mapToMyHospitalResponse(UserHospitalMembership membership) {
        Hospital hospital = membership.getHospital();
        return MyHospitalResponse.builder()
                .hospitalId(hospital.getHospitalId())
                .name(hospital.getName())
                .code(hospital.getCode())
                .hospitalRole(membership.getHospitalRole())
                .membershipStatus(membership.getStatus())
                .build();
    }
}
