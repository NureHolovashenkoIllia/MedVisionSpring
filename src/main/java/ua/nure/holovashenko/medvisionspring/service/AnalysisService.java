package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.ImageAnalysisResponse;
import ua.nure.holovashenko.medvisionspring.entity.Doctor;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.DoctorRepository;
import ua.nure.holovashenko.medvisionspring.repository.ImageAnalysisRepository;
import ua.nure.holovashenko.medvisionspring.repository.PatientRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ImageAnalysisRepository imageAnalysisRepository;
    private final UserRepository userRepository;

    public List<ImageAnalysisResponse> getAllAnalyses() {
        return imageAnalysisRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<ImageAnalysisResponse> getAnalysisById(Long id) {
        return imageAnalysisRepository.findById(id).map(this::mapToDto);
    }

    public List<ImageAnalysisResponse> getAnalysesByDoctorId(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException("Лікаря не знайдено", HttpStatus.NOT_FOUND));

        if (doctor.getUserRole() != UserRole.DOCTOR) {
            throw new ApiException("Лікаря не знайдено", HttpStatus.NOT_FOUND);
        }

        return imageAnalysisRepository.findAllByDoctor(doctor).stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<ImageAnalysisResponse> getAnalysesByPatientId(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Пацієнта не знайдено", HttpStatus.NOT_FOUND));

        if (patient.getUserRole() != UserRole.PATIENT) {
            throw new ApiException("Пацієнта не знайдено", HttpStatus.NOT_FOUND);
        }

        return imageAnalysisRepository.findAllByPatient(patient).stream()
                .map(this::mapToDto)
                .toList();
    }

    public AnalysisStatus getStatusById(Long analysisId) {
        return imageAnalysisRepository.findById(analysisId)
                .map(ImageAnalysis::getAnalysisStatus)
                .orElseThrow(() -> new ApiException("Аналіз не знайдено", HttpStatus.NOT_FOUND));
    }

    public void updateStatus(Long analysisId, AnalysisStatus newStatus) {
        ImageAnalysis analysis = imageAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ApiException("Аналіз не знайдено", HttpStatus.NOT_FOUND));

        analysis.setAnalysisStatus(newStatus);
        imageAnalysisRepository.save(analysis);
    }

    public boolean deleteAnalysis(Long id) {
        if (!imageAnalysisRepository.existsById(id)) {
            return false;
        }
        imageAnalysisRepository.deleteById(id);
        return true;
    }

    private ImageAnalysisResponse mapToDto(ImageAnalysis analysis) {
        ImageAnalysisResponse dto = new ImageAnalysisResponse();
        dto.setImageAnalysisId(analysis.getImageAnalysisId());
        dto.setAnalysisAccuracy(analysis.getAnalysisAccuracy());
        dto.setAnalysisPrecision(analysis.getAnalysisPrecision());
        dto.setAnalysisRecall(analysis.getAnalysisRecall());
        dto.setAnalysisDiagnosis(analysis.getAnalysisDiagnosis());
        dto.setCreationDatetime(analysis.getCreationDatetime());
        dto.setAnalysisStatus(analysis.getAnalysisStatus());

        if (analysis.getImageFile() != null) {
            dto.setImageFileId(analysis.getImageFile().getImageFileId());
        }
        if (analysis.getHeatmapFile() != null) {
            dto.setHeatmapFileId(analysis.getHeatmapFile().getImageFileId());
        }
        if (analysis.getPatient() != null) {
            dto.setPatientId(analysis.getPatient().getUserId());
        }
        if (analysis.getDoctor() != null) {
            dto.setDoctorId(analysis.getDoctor().getUserId());
        }

        return dto;
    }
}
