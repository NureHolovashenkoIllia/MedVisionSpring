package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.ComparisonReport;
import ua.nure.holovashenko.medvisionspring.dto.ImageAnalysisResponse;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.ImageAnalysisRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;
import ua.nure.holovashenko.medvisionspring.svm.HeatmapGenerator;
import ua.nure.holovashenko.medvisionspring.svm.ImageUtils;
import ua.nure.holovashenko.medvisionspring.svm.SvmModelManager;
import ua.nure.holovashenko.medvisionspring.util.pdf.PdfComparisonReportUtil;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ImageAnalysisRepository imageAnalysisRepository;
    private final UserRepository userRepository;
    private final SvmModelManager modelManager;
    private final HeatmapGenerator heatmapGenerator;

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

    public ComparisonReport compareAnalyses(Long fromId, Long toId) {
        ImageAnalysis from = imageAnalysisRepository.findById(fromId)
                .orElseThrow(() -> new ApiException("Аналіз FROM не знайдено", HttpStatus.NOT_FOUND));
        ImageAnalysis to = imageAnalysisRepository.findById(toId)
                .orElseThrow(() -> new ApiException("Аналіз TO не знайдено", HttpStatus.NOT_FOUND));

        // Отримати діагнози
        int diagnosisClassFrom = from.getDiagnosisClass();
        int diagnosisClassTo = to.getDiagnosisClass();

        // Отримати теплові карти
        double[][] heatmapFrom = modelManager.getHeatmapData(from.getImageFile().getImageFileUrl(), true);
        double[][] heatmapTo = modelManager.getHeatmapData(to.getImageFile().getImageFileUrl(), true);
        double[][] diffMap = subtractHeatmaps(heatmapFrom, heatmapTo);
        Mat diffHeatmapMat = heatmapGenerator.generateHeatmap(null, diffMap);
        String encodedDiffHeatmap = ImageUtils.encode(diffHeatmapMat);

        Mat fromImage = ImageUtils.loadImage(from.getImageFile().getImageFileUrl());
        Mat toImage = ImageUtils.loadImage(to.getImageFile().getImageFileUrl());

        String fromImageBase64 = ImageUtils.encode(fromImage);
        String toImageBase64 = ImageUtils.encode(toImage);

        // Створити DTO
        ComparisonReport report = new ComparisonReport();
        report.setFromId(fromId);
        report.setToId(toId);
        report.setDiagnosisClassFrom(diagnosisClassFrom);
        report.setDiagnosisClassTo(diagnosisClassTo);

        report.setAnalysisDetailsFrom(from.getAnalysisDetails());
        report.setAnalysisDetailsTo(to.getAnalysisDetails());

        report.setDiagnosisTextFrom(from.getAnalysisDiagnosis());
        report.setDiagnosisTextTo(to.getAnalysisDiagnosis());

        report.setTreatmentRecommendationsFrom(from.getTreatmentRecommendations());
        report.setTreatmentRecommendationsTo(to.getTreatmentRecommendations());

        report.setDiffHeatmap(encodedDiffHeatmap);
        report.setFromImageBase64(fromImageBase64);
        report.setToImageBase64(toImageBase64);
        report.setAccuracyFrom(from.getAnalysisAccuracy());
        report.setAccuracyTo(to.getAnalysisAccuracy());
        report.setPrecisionFrom(from.getAnalysisPrecision());
        report.setPrecisionTo(to.getAnalysisPrecision());
        report.setRecallFrom(from.getAnalysisRecall());
        report.setRecallTo(to.getAnalysisRecall());
        report.setCreatedAtFrom(from.getCreationDatetime().toString());
        report.setCreatedAtTo(to.getCreationDatetime().toString());

        return report;
    }

    private double[][] subtractHeatmaps(double[][] heatmapA, double[][] heatmapB) {
        int rows = heatmapA.length;
        int cols = heatmapA[0].length;
        double[][] diff = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                diff[i][j] = heatmapA[i][j] - heatmapB[i][j];
            }
        }
        return diff;
    }

    public byte[] exportComparisonToPdf(ComparisonReport report) {
        return PdfComparisonReportUtil.generateComparisonPdf(report);
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
        dto.setAnalysisDetails(analysis.getAnalysisDetails());
        dto.setAnalysisDiagnosis(analysis.getAnalysisDiagnosis());
        dto.setTreatmentRecommendations(analysis.getTreatmentRecommendations());
        dto.setDiagnosisClass(analysis.getDiagnosisClass());
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
