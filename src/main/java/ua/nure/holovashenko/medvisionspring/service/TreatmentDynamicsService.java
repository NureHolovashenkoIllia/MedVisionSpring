package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TreatmentDynamicsService {

    private final HospitalRepository hospitalRepository;
    private final TreatmentRepository treatmentRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final AnalysisComparisonRepository comparisonRepository;
    private final TreatmentConclusionRepository conclusionRepository;
    private final UserRepository userRepository;
    private final HospitalAccessPolicy accessPolicy;

    @Transactional
    public AnalysisComparisonResponse createComparison(
            Long hospitalId,
            Long treatmentId,
            AnalysisComparisonCreateRequest request,
            UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        Treatment treatment = getTreatment(hospitalId, treatmentId);
        requireWriteAccess(currentUser, treatment);

        ImageAnalysis from = getAnalysisInTreatment(request.getFromAnalysisId(), treatment);
        ImageAnalysis to = getAnalysisInTreatment(request.getToAnalysisId(), treatment);

        AnalysisComparison comparison = comparisonRepository.save(AnalysisComparison.builder()
                .hospital(treatment.getHospital())
                .treatment(treatment)
                .fromAnalysis(from)
                .toAnalysis(to)
                .diagnosisClassFrom(from.getDiagnosisClass())
                .diagnosisClassTo(to.getDiagnosisClass())
                .diagnosisChanged(!Objects.equals(from.getDiagnosisClass(), to.getDiagnosisClass()))
                .accuracyDelta(delta(to.getAnalysisAccuracy(), from.getAnalysisAccuracy()))
                .precisionDelta(delta(to.getAnalysisPrecision(), from.getAnalysisPrecision()))
                .recallDelta(delta(to.getAnalysisRecall(), from.getAnalysisRecall()))
                .doctorNotes(request.getDoctorNotes())
                .createdBy(currentUser)
                .build());

        return mapComparison(comparison);
    }

    public List<AnalysisComparisonResponse> getComparisons(Long hospitalId, Long treatmentId, UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        Treatment treatment = getTreatment(hospitalId, treatmentId);
        requireReadAccess(currentUser, treatment);

        return comparisonRepository.findAllByTreatmentOrderByCreatedAtAsc(treatment).stream()
                .map(this::mapComparison)
                .toList();
    }

    @Transactional
    public TreatmentConclusionResponse upsertConclusion(
            Long hospitalId,
            Long treatmentId,
            TreatmentConclusionRequest request,
            UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        Treatment treatment = getTreatment(hospitalId, treatmentId);
        requireWriteAccess(currentUser, treatment);

        TreatmentConclusion conclusion = conclusionRepository.findByTreatment(treatment)
                .map(existing -> {
                    existing.setConclusionText(request.getConclusionText());
                    existing.setRecommendations(request.getRecommendations());
                    existing.setUpdatedBy(currentUser);
                    return existing;
                })
                .orElseGet(() -> TreatmentConclusion.builder()
                        .hospital(treatment.getHospital())
                        .treatment(treatment)
                        .conclusionText(request.getConclusionText())
                        .recommendations(request.getRecommendations())
                        .createdBy(currentUser)
                        .updatedBy(currentUser)
                        .build());

        return mapConclusion(conclusionRepository.save(conclusion));
    }

    public TreatmentConclusionResponse getConclusion(Long hospitalId, Long treatmentId, UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        Treatment treatment = getTreatment(hospitalId, treatmentId);
        requireReadAccess(currentUser, treatment);

        return conclusionRepository.findByTreatment(treatment)
                .map(this::mapConclusion)
                .orElseThrow(() -> new ApiException("Висновок лікування не знайдено", HttpStatus.NOT_FOUND));
    }

    public TreatmentReportResponse getReport(Long hospitalId, Long treatmentId, UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        Treatment treatment = getTreatment(hospitalId, treatmentId);
        requireReadAccess(currentUser, treatment);

        List<ImageAnalysisResponse> analyses = imageAnalysisRepository.findAllByTreatmentOrderByCreationDatetimeAsc(treatment).stream()
                .map(this::mapAnalysis)
                .toList();

        List<AnalysisComparisonResponse> comparisons = comparisonRepository.findAllByTreatmentOrderByCreatedAtAsc(treatment).stream()
                .map(this::mapComparison)
                .toList();

        TreatmentConclusionResponse conclusion = conclusionRepository.findByTreatment(treatment)
                .map(this::mapConclusion)
                .orElse(null);

        return TreatmentReportResponse.builder()
                .treatment(mapTreatment(treatment, analyses.size()))
                .analyses(analyses)
                .comparisons(comparisons)
                .conclusion(conclusion)
                .build();
    }

    private Treatment getTreatment(Long hospitalId, Long treatmentId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ApiException("Лікарню не знайдено", HttpStatus.NOT_FOUND));
        return treatmentRepository.findByTreatmentIdAndHospital(treatmentId, hospital)
                .orElseThrow(() -> new ApiException("Лікування не знайдено", HttpStatus.NOT_FOUND));
    }

    private ImageAnalysis getAnalysisInTreatment(Long analysisId, Treatment treatment) {
        ImageAnalysis analysis = imageAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ApiException("Аналіз не знайдено", HttpStatus.NOT_FOUND));

        if (analysis.getHospital() == null
                || analysis.getTreatment() == null
                || !Objects.equals(analysis.getHospital().getHospitalId(), treatment.getHospital().getHospitalId())
                || !Objects.equals(analysis.getTreatment().getTreatmentId(), treatment.getTreatmentId())) {
            throw new ApiException("Аналіз не належить цьому лікуванню", HttpStatus.BAD_REQUEST);
        }

        return analysis;
    }

    private void requireReadAccess(User user, Treatment treatment) {
        accessPolicy.requireHospitalAccess(user, treatment.getHospital());
        if (!accessPolicy.canManageHospital(user, treatment.getHospital())
                && !Objects.equals(treatment.getPatient().getUserId(), user.getUserId())
                && !Objects.equals(treatment.getPrimaryDoctor().getUserId(), user.getUserId())) {
            throw new ApiException("Немає доступу до лікування", HttpStatus.FORBIDDEN);
        }
    }

    private void requireWriteAccess(User user, Treatment treatment) {
        accessPolicy.requireHospitalAccess(user, treatment.getHospital());
        if (user.getUserRole() == UserRole.PATIENT) {
            throw new ApiException("Пацієнт не може змінювати динаміку лікування", HttpStatus.FORBIDDEN);
        }
        if (!accessPolicy.canManageHospital(user, treatment.getHospital())
                && !Objects.equals(treatment.getPrimaryDoctor().getUserId(), user.getUserId())) {
            throw new ApiException("Немає прав на зміну лікування", HttpStatus.FORBIDDEN);
        }
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    private Float delta(Float to, Float from) {
        if (to == null || from == null) {
            return null;
        }
        return to - from;
    }

    private AnalysisComparisonResponse mapComparison(AnalysisComparison comparison) {
        User createdBy = comparison.getCreatedBy();
        return AnalysisComparisonResponse.builder()
                .comparisonId(comparison.getAnalysisComparisonId())
                .hospitalId(comparison.getHospital().getHospitalId())
                .treatmentId(comparison.getTreatment().getTreatmentId())
                .fromAnalysisId(comparison.getFromAnalysis().getImageAnalysisId())
                .toAnalysisId(comparison.getToAnalysis().getImageAnalysisId())
                .diagnosisClassFrom(comparison.getDiagnosisClassFrom())
                .diagnosisClassTo(comparison.getDiagnosisClassTo())
                .diagnosisChanged(comparison.isDiagnosisChanged())
                .accuracyDelta(comparison.getAccuracyDelta())
                .precisionDelta(comparison.getPrecisionDelta())
                .recallDelta(comparison.getRecallDelta())
                .doctorNotes(comparison.getDoctorNotes())
                .createdByUserId(createdBy.getUserId())
                .createdByUserName(createdBy.getUserName())
                .createdAt(comparison.getCreatedAt())
                .build();
    }

    private TreatmentConclusionResponse mapConclusion(TreatmentConclusion conclusion) {
        return TreatmentConclusionResponse.builder()
                .conclusionId(conclusion.getTreatmentConclusionId())
                .hospitalId(conclusion.getHospital().getHospitalId())
                .treatmentId(conclusion.getTreatment().getTreatmentId())
                .conclusionText(conclusion.getConclusionText())
                .recommendations(conclusion.getRecommendations())
                .createdByUserId(conclusion.getCreatedBy().getUserId())
                .createdByUserName(conclusion.getCreatedBy().getUserName())
                .updatedByUserId(conclusion.getUpdatedBy().getUserId())
                .updatedByUserName(conclusion.getUpdatedBy().getUserName())
                .createdAt(conclusion.getCreatedAt())
                .updatedAt(conclusion.getUpdatedAt())
                .build();
    }

    private TreatmentResponse mapTreatment(Treatment treatment, int analysisCount) {
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
                .analysisCount(analysisCount)
                .build();
    }

    private ImageAnalysisResponse mapAnalysis(ImageAnalysis analysis) {
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
}
