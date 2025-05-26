package ua.nure.holovashenko.medvisionspring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;
import ua.nure.holovashenko.medvisionspring.svm.MetricsCalculator;
import ua.nure.holovashenko.medvisionspring.svm.ModelMetrics;
import ua.nure.holovashenko.medvisionspring.svm.SvmService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ua.nure.holovashenko.medvisionspring.svm.SvmService.CLASS_LABELS;

@Service
@RequiredArgsConstructor
public class DoctorAnalysisService {

    private final SvmService svmService;
    private final GcsService gcsService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final ImageFileRepository imageFileRepository;
    private final AnalysisNoteRepository analysisNoteRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ImageAnalysis analyzeAndSave(MultipartFile file, Long patientId, Long doctorId) throws IOException {
        File tempFile = File.createTempFile("upload-", ".png");
        file.transferTo(tempFile);

        User patientUser = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Пацієнт не знайдений", HttpStatus.NOT_FOUND));
        User doctorUser = userRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException("Лікар не знайдений", HttpStatus.NOT_FOUND));


        int prediction = svmService.classify(tempFile, false);

        String imageObjectName = "images/upload-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        String imageUrl = gcsService.uploadFile(tempFile, imageObjectName, file.getContentType());

        ImageFile imageFile = ImageFile.builder()
                .imageFileName(imageObjectName)
                .imageFileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .imageFileUrl(imageUrl)
                .uploadedBy(doctorUser)
                .build();
        imageFileRepository.save(imageFile);

        var heatmapMat = svmService.generateHeatmap(tempFile, true);
        File heatmapFile = File.createTempFile("heatmap-", ".png");
        svmService.saveMatToFile(heatmapMat, heatmapFile);

        String heatmapObjectName = "heatmaps/heatmap-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        String heatmapUrl = gcsService.uploadFile(heatmapFile, heatmapObjectName, file.getContentType());

        ImageFile heatmapImage = ImageFile.builder()
                .imageFileName(heatmapObjectName)
                .imageFileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .imageFileUrl(heatmapUrl)
                .uploadedBy(doctorUser)
                .build();
        imageFileRepository.save(heatmapImage);

        ModelMetrics metrics = svmService.loadMetrics("svm-metrics/full_metrics.json");
        MetricsCalculator.ClassMetrics classMetrics = metrics.perClassMetrics().get(prediction);
        float precision = classMetrics != null ? (float) classMetrics.precision() : 0f;
        float recall = classMetrics != null ? (float) classMetrics.recall() : 0f;

        String diagnosisText = CLASS_LABELS.getOrDefault(prediction, "Не вдалося точно визначити діагноз — результат невідомий.");

        ImageAnalysis analysis = ImageAnalysis.builder()
                .imageFile(imageFile)
                .heatmapFile(heatmapImage)
                .analysisDiagnosis(diagnosisText)
                .analysisAccuracy((float) metrics.accuracy())
                .analysisPrecision(precision)
                .analysisRecall(recall)
                .creationDatetime(LocalDateTime.now())
                .analysisStatus(AnalysisStatus.REQUIRES_REVISION)
                .diagnosisClass(prediction)
                .patient(patientUser)
                .doctor(doctorUser)
                .build();

        ImageAnalysis savedAnalysis = imageAnalysisRepository.save(analysis);

        Doctor doctor = doctorRepository.findById(doctorUser.getUserId()).orElse(null);

        DiagnosisHistory diagnosis = DiagnosisHistory.builder()
                .imageAnalysis(savedAnalysis)
                .diagnosisText(savedAnalysis.getAnalysisDiagnosis())
                .changedByDoctor(doctor)
                .changeReason("Діагноз SVM")
                .build();

        diagnosisHistoryRepository.save(diagnosis);

        return savedAnalysis;
    }

    public Optional<ImageAnalysis> getAnalysis(Long id) {
        return imageAnalysisRepository.findById(id);
    }

    public Optional<byte[]> getHeatmapBytes(Long id) throws IOException {
        return imageAnalysisRepository.findById(id).map(a -> {
            try {
                return gcsService.downloadFile(a.getHeatmapFile().getImageFileUrl());
            } catch (IOException e) {
                throw new RuntimeException("Cannot read heatmap from GCS", e);
            }
        });
    }

    public boolean updateDiagnosis(Long id, String diagnosis) {
        return imageAnalysisRepository.findById(id).map(a -> {
            a.setAnalysisDiagnosis(diagnosis);
            imageAnalysisRepository.save(a);
            return true;
        }).orElse(false);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public boolean addNote(Long analysesId, Long doctorId, AddNoteRequest inputNote) {
        try {
            ImageAnalysis analysis = imageAnalysisRepository.findById(analysesId)
                    .orElseThrow(() -> new IllegalArgumentException("Image analysis not found"));
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

            AnalysisNote note = AnalysisNote.builder()
                    .noteText(inputNote.getNoteText())
                    .noteAreaX(inputNote.getNoteAreaX())
                    .noteAreaY(inputNote.getNoteAreaY())
                    .noteAreaWidth(inputNote.getNoteAreaWidth())
                    .noteAreaHeight(inputNote.getNoteAreaHeight())
                    .creationDatetime(LocalDateTime.now())
                    .doctor(doctor)
                    .imageAnalysis(analysis)
                    .build();

            analysisNoteRepository.save(note);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
