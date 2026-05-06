package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final TreatmentRepository treatmentRepository;
    private final TreatmentStatusHistoryRepository statusHistoryRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final HospitalAccessPolicy accessPolicy;

    @Transactional
    public TreatmentResponse createTreatment(Long hospitalId, TreatmentCreateRequest request, UserDetails userDetails) {
        User doctor = getUserOrThrow(userDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalAccess(doctor, hospital);

        if (!accessPolicy.hasActiveRole(doctor, hospital, HospitalRole.DOCTOR) && !accessPolicy.isGlobalAdmin(doctor)) {
            throw new ApiException("Тільки лікар може відкрити лікування", HttpStatus.FORBIDDEN);
        }

        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ApiException("Пацієнта не знайдено", HttpStatus.NOT_FOUND));

        if (patient.getUserRole() != UserRole.PATIENT) {
            throw new ApiException("Користувач не є пацієнтом", HttpStatus.BAD_REQUEST);
        }

        accessPolicy.requireHospitalAccess(patient, hospital);

        Treatment treatment = Treatment.builder()
                .hospital(hospital)
                .patient(patient)
                .primaryDoctor(doctor)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TreatmentStatus.OPEN)
                .openedAt(LocalDateTime.now())
                .createdBy(doctor)
                .build();

        Treatment saved = treatmentRepository.save(treatment);
        saveStatusHistory(saved, null, TreatmentStatus.OPEN, doctor, "Лікування відкрито");
        return mapToResponse(saved);
    }

    public List<TreatmentResponse> getTreatments(Long hospitalId, Long patientId, Long doctorId, TreatmentStatus status, UserDetails userDetails) {
        User currentUser = getUserOrThrow(userDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalAccess(currentUser, hospital);

        List<Treatment> treatments;
        if (patientId != null) {
            User patient = userRepository.findById(patientId)
                    .orElseThrow(() -> new ApiException("Пацієнта не знайдено", HttpStatus.NOT_FOUND));
            treatments = treatmentRepository.findAllByHospitalAndPatient(hospital, patient);
        } else if (doctorId != null) {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new ApiException("Лікаря не знайдено", HttpStatus.NOT_FOUND));
            treatments = treatmentRepository.findAllByHospitalAndPrimaryDoctor(hospital, doctor);
        } else if (status != null) {
            treatments = treatmentRepository.findAllByHospitalAndStatus(hospital, status);
        } else {
            treatments = treatmentRepository.findAllByHospital(hospital);
        }

        return treatments.stream()
                .filter(treatment -> canReadTreatment(currentUser, treatment))
                .map(this::mapToResponse)
                .toList();
    }

    public TreatmentTimelineResponse getTreatment(Long hospitalId, Long treatmentId, UserDetails userDetails) {
        User currentUser = getUserOrThrow(userDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalAccess(currentUser, hospital);
        Treatment treatment = getTreatmentOrThrow(treatmentId, hospital);
        requireTreatmentReadAccess(currentUser, treatment);

        return TreatmentTimelineResponse.builder()
                .treatment(mapToResponse(treatment))
                .analyses(imageAnalysisRepository.findAllByTreatmentOrderByCreationDatetimeAsc(treatment).stream()
                        .map(this::mapAnalysisToResponse)
                        .toList())
                .statusHistory(statusHistoryRepository.findAllByTreatmentOrderByChangedAtAsc(treatment).stream()
                        .map(this::mapStatusHistory)
                        .toList())
                .build();
    }

    @Transactional
    public TreatmentResponse closeTreatment(Long hospitalId, Long treatmentId, TreatmentCloseRequest request, UserDetails userDetails) {
        User currentUser = getUserOrThrow(userDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalAccess(currentUser, hospital);
        Treatment treatment = getTreatmentOrThrow(treatmentId, hospital);
        requireTreatmentWriteAccess(currentUser, treatment);

        if (treatment.getStatus() == TreatmentStatus.CLOSED) {
            throw new ApiException("Лікування вже закрите", HttpStatus.BAD_REQUEST);
        }

        TreatmentStatus previous = treatment.getStatus();
        treatment.setStatus(TreatmentStatus.CLOSED);
        treatment.setClosedAt(LocalDateTime.now());
        treatment.setCloseReason(request.getCloseReason());

        Treatment saved = treatmentRepository.save(treatment);
        saveStatusHistory(saved, previous, TreatmentStatus.CLOSED, currentUser, request.getCloseReason());
        return mapToResponse(saved);
    }

    public Treatment getTreatmentForAnalysis(Long hospitalId, Long treatmentId, UserDetails userDetails) {
        User currentUser = getUserOrThrow(userDetails);
        Hospital hospital = getHospitalOrThrow(hospitalId);
        accessPolicy.requireHospitalAccess(currentUser, hospital);
        Treatment treatment = getTreatmentOrThrow(treatmentId, hospital);
        requireTreatmentWriteAccess(currentUser, treatment);

        if (treatment.getStatus() == TreatmentStatus.CLOSED || treatment.getStatus() == TreatmentStatus.CANCELLED) {
            throw new ApiException("Не можна додати аналіз у закрите або скасоване лікування", HttpStatus.BAD_REQUEST);
        }

        return treatment;
    }

    public Treatment getTreatmentOrThrow(Long treatmentId, Hospital hospital) {
        return treatmentRepository.findByTreatmentIdAndHospital(treatmentId, hospital)
                .orElseThrow(() -> new ApiException("Лікування не знайдено", HttpStatus.NOT_FOUND));
    }

    private boolean canReadTreatment(User user, Treatment treatment) {
        return accessPolicy.canManageHospital(user, treatment.getHospital())
                || treatment.getPatient().getUserId().equals(user.getUserId())
                || treatment.getPrimaryDoctor().getUserId().equals(user.getUserId());
    }

    private void requireTreatmentReadAccess(User user, Treatment treatment) {
        if (!canReadTreatment(user, treatment)) {
            throw new ApiException("Немає доступу до лікування", HttpStatus.FORBIDDEN);
        }
    }

    private void requireTreatmentWriteAccess(User user, Treatment treatment) {
        if (!accessPolicy.canManageHospital(user, treatment.getHospital())
                && !treatment.getPrimaryDoctor().getUserId().equals(user.getUserId())) {
            throw new ApiException("Немає прав на зміну лікування", HttpStatus.FORBIDDEN);
        }
    }

    private Hospital getHospitalOrThrow(Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ApiException("Лікарню не знайдено", HttpStatus.NOT_FOUND));
    }

    private User getUserOrThrow(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    private void saveStatusHistory(Treatment treatment, TreatmentStatus from, TreatmentStatus to, User changedBy, String reason) {
        statusHistoryRepository.save(TreatmentStatusHistory.builder()
                .treatment(treatment)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build());
    }

    private TreatmentResponse mapToResponse(Treatment treatment) {
        return TreatmentResponse.builder()
                .treatmentId(treatment.getTreatmentId())
                .hospitalId(treatment.getHospital().getHospitalId())
                .hospitalName(treatment.getHospital().getName())
                .patientId(treatment.getPatient().getUserId())
                .patientName(treatment.getPatient().getUserName())
                .primaryDoctorId(treatment.getPrimaryDoctor().getUserId())
                .primaryDoctorName(treatment.getPrimaryDoctor().getUserName())
                .title(treatment.getTitle())
                .description(treatment.getDescription())
                .status(treatment.getStatus())
                .openedAt(treatment.getOpenedAt())
                .closedAt(treatment.getClosedAt())
                .closeReason(treatment.getCloseReason())
                .analysisCount(imageAnalysisRepository.findAllByTreatmentOrderByCreationDatetimeAsc(treatment).size())
                .build();
    }

    private ImageAnalysisResponse mapAnalysisToResponse(ImageAnalysis analysis) {
        ImageAnalysisResponse dto = new ImageAnalysisResponse();
        dto.setImageAnalysisId(analysis.getImageAnalysisId());
        dto.setAnalysisAccuracy(analysis.getAnalysisAccuracy());
        dto.setAnalysisPrecision(analysis.getAnalysisPrecision());
        dto.setAnalysisRecall(analysis.getAnalysisRecall());
        dto.setAnalysisDetails(analysis.getAnalysisDetails());
        dto.setAnalysisDiagnosis(analysis.getAnalysisDiagnosis());
        dto.setTreatmentRecommendations(analysis.getTreatmentRecommendations());
        dto.setCreationDatetime(analysis.getCreationDatetime());
        dto.setAnalysisStatus(analysis.getAnalysisStatus());
        dto.setDiagnosisClass(analysis.getDiagnosisClass());
        dto.setImageFileId(analysis.getImageFile() != null ? analysis.getImageFile().getImageFileId() : null);
        dto.setHeatmapFileId(analysis.getHeatmapFile() != null ? analysis.getHeatmapFile().getImageFileId() : null);
        dto.setPatientId(analysis.getPatient() != null ? analysis.getPatient().getUserId() : null);
        dto.setDoctorId(analysis.getDoctor() != null ? analysis.getDoctor().getUserId() : null);
        dto.setHospitalId(analysis.getHospital() != null ? analysis.getHospital().getHospitalId() : null);
        dto.setTreatmentId(analysis.getTreatment() != null ? analysis.getTreatment().getTreatmentId() : null);
        dto.setAnalysisJobId(analysis.getAnalysisJob() != null ? analysis.getAnalysisJob().getAnalysisJobId() : null);
        dto.setModelVersionId(analysis.getModelVersion() != null ? analysis.getModelVersion().getModelVersionId() : null);
        dto.setModelVersion(analysis.getModelVersion() != null ? analysis.getModelVersion().getVersion() : null);
        return dto;
    }

    private TreatmentStatusHistoryResponse mapStatusHistory(TreatmentStatusHistory history) {
        return TreatmentStatusHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedByUserId(history.getChangedBy().getUserId())
                .changedByUserName(history.getChangedBy().getUserName())
                .reason(history.getReason())
                .changedAt(history.getChangedAt())
                .build();
    }
}
