package ua.nure.holovashenko.medvisionspring.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.Treatment;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TreatmentServiceTests {

    private final HospitalRepository hospitalRepository = mock(HospitalRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TreatmentRepository treatmentRepository = mock(TreatmentRepository.class);
    private final TreatmentStatusHistoryRepository statusHistoryRepository = mock(TreatmentStatusHistoryRepository.class);
    private final ImageAnalysisRepository imageAnalysisRepository = mock(ImageAnalysisRepository.class);
    private final HospitalAccessPolicy accessPolicy = mock(HospitalAccessPolicy.class);

    private final TreatmentService treatmentService = new TreatmentService(
            hospitalRepository,
            userRepository,
            treatmentRepository,
            statusHistoryRepository,
            imageAnalysisRepository,
            accessPolicy
    );

    @Test
    void rejectsAnalysisForClosedTreatment() {
        Hospital hospital = Hospital.builder().hospitalId(1L).build();
        User doctor = User.builder()
                .userId(10L)
                .email("doctor@example.com")
                .userRole(UserRole.DOCTOR)
                .build();
        Treatment treatment = Treatment.builder()
                .treatmentId(100L)
                .hospital(hospital)
                .primaryDoctor(doctor)
                .status(TreatmentStatus.CLOSED)
                .build();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("doctor@example.com")
                .password("password")
                .roles("DOCTOR")
                .build();

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(treatmentRepository.findByTreatmentIdAndHospital(100L, hospital)).thenReturn(Optional.of(treatment));

        assertThatThrownBy(() -> treatmentService.getTreatmentForAnalysis(1L, 100L, userDetails))
                .isInstanceOf(ApiException.class)
                .hasMessage("Не можна додати аналіз у закрите або скасоване лікування");
    }
}
