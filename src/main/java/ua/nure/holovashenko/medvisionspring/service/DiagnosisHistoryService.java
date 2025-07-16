package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.DiagnosisHistoryRequest;
import ua.nure.holovashenko.medvisionspring.dto.DiagnosisHistoryResponse;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisHistoryService {

    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final DoctorRepository doctorRepository;

    public DiagnosisHistoryResponse getById(Long id) {
        DiagnosisHistory diagnosis = diagnosisHistoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Diagnosis not found: " + id, HttpStatus.NOT_FOUND));

        return mapToDto(diagnosis);
    }

    public List<DiagnosisHistoryResponse> getAllByAnalysisId(Long analysisId) {
        return diagnosisHistoryRepository
                .findByImageAnalysis_ImageAnalysisIdOrderByChangeDatetimeDesc(analysisId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DiagnosisHistoryResponse createDiagnosis(DiagnosisHistoryRequest request) {
        ImageAnalysis analysis = imageAnalysisRepository.findById(request.getAnalysisId())
                .orElseThrow(() -> new ApiException("Analysis not found: " + request.getAnalysisId(), HttpStatus.NOT_FOUND));
        Doctor doctor = request.getDoctorId() != null
                ? doctorRepository.findById(request.getDoctorId()).orElse(null)
                : null;

        DiagnosisHistory diagnosis = DiagnosisHistory.builder()
                .imageAnalysis(analysis)
                .diagnosisText(request.getDiagnosisText())
                .changedByDoctor(doctor)
                .changeReason(request.getReason())
                .analysisDetails(request.getAnalysisDetails())
                .treatmentRecommendations(request.getTreatmentRecommendations())
                .build();

        // оновити поточний діагноз у image_analysis
        analysis.setAnalysisDiagnosis(request.getDiagnosisText());
        analysis.setAnalysisDetails(request.getAnalysisDetails());
        analysis.setTreatmentRecommendations(request.getTreatmentRecommendations());
        imageAnalysisRepository.save(analysis);

        return mapToDto(diagnosisHistoryRepository.save(diagnosis));
    }

    private DiagnosisHistoryResponse mapToDto(DiagnosisHistory entity) {
        return DiagnosisHistoryResponse.builder()
                .id(entity.getDiagnosisHistoryId())
                .diagnosisText(entity.getDiagnosisText())
                .doctorName(entity.getChangedByDoctor() != null
                        ? entity.getChangedByDoctor().getUser().getUserName()
                        : null)
                .reason(entity.getChangeReason())
                .timestamp(entity.getChangeDatetime())
                .analysisDetails(entity.getAnalysisDetails())
                .treatmentRecommendations(entity.getTreatmentRecommendations())
                .build();
    }
}
