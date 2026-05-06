package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.Treatment;
import ua.nure.holovashenko.medvisionspring.entity.TreatmentStatusHistory;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.HospitalRepository;
import ua.nure.holovashenko.medvisionspring.repository.TreatmentRepository;
import ua.nure.holovashenko.medvisionspring.repository.TreatmentStatusHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LegacyTreatmentResolver {

    private static final String LEGACY_HOSPITAL_CODE = "LEGACY";

    private final HospitalRepository hospitalRepository;
    private final TreatmentRepository treatmentRepository;
    private final TreatmentStatusHistoryRepository statusHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Treatment resolve(User patient, User doctor) {
        Hospital legacyHospital = hospitalRepository.findByCode(LEGACY_HOSPITAL_CODE)
                .orElseThrow(() -> new ApiException("Legacy hospital не знайдено", HttpStatus.INTERNAL_SERVER_ERROR));

        return treatmentRepository.findByHospitalAndPatientAndPrimaryDoctorAndStatus(
                        legacyHospital,
                        patient,
                        doctor,
                        TreatmentStatus.OPEN
                )
                .orElseGet(() -> createLegacyTreatment(legacyHospital, patient, doctor));
    }

    private Treatment createLegacyTreatment(Hospital hospital, User patient, User doctor) {
        Treatment treatment = treatmentRepository.save(Treatment.builder()
                .hospital(hospital)
                .patient(patient)
                .primaryDoctor(doctor)
                .title("Legacy treatment")
                .description("Automatically created for legacy analysis endpoint.")
                .status(TreatmentStatus.OPEN)
                .openedAt(LocalDateTime.now())
                .createdBy(doctor)
                .build());

        statusHistoryRepository.save(TreatmentStatusHistory.builder()
                .treatment(treatment)
                .fromStatus(null)
                .toStatus(TreatmentStatus.OPEN)
                .changedBy(doctor)
                .reason("Legacy endpoint auto-created treatment")
                .changedAt(LocalDateTime.now())
                .build());

        return treatment;
    }
}
