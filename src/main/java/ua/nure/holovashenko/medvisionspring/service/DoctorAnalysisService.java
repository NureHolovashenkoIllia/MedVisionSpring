package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;
import ua.nure.holovashenko.medvisionspring.svm.ModelMetrics;
import ua.nure.holovashenko.medvisionspring.svm.SvmService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAnalysisService {

    private final SvmService svmService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final ImageFileRepository imageFileRepository;
    private final AnalysisNoteRepository analysisNoteRepository;

    @Transactional
    public ImageAnalysis analyzeAndSave(MultipartFile file, Long patientId, Long doctorId) throws IOException {
        File tempFile = File.createTempFile("upload-", ".png");
        file.transferTo(tempFile);

        User patient = userRepository.findById(patientId).orElseThrow();
        User doctor = userRepository.findById(doctorId).orElseThrow();

        int prediction = svmService.classify(tempFile, false);

        ImageFile imageFile = ImageFile.builder()
                .imageFileName("upload-" + file.getOriginalFilename())
                .imageFileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .imageFileUrl(tempFile.getAbsolutePath())
                .uploadedBy(doctor)
                .build();
        imageFileRepository.save(imageFile);

        var heatmapMat = svmService.generateHeatmap(tempFile, true);
        File heatmapFile = File.createTempFile("heatmap-", ".png");
        svmService.saveMatToFile(heatmapMat, heatmapFile);

        ImageFile heatmapImage = ImageFile.builder()
                .imageFileName("heatmap-" + file.getOriginalFilename())
                .imageFileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .imageFileUrl(heatmapFile.getAbsolutePath())
                .uploadedBy(doctor)
                .build();
        imageFileRepository.save(heatmapImage);

        ModelMetrics metrics = svmService.loadMetrics("model/metrics.json");

        ImageAnalysis analysis = ImageAnalysis.builder()
                .imageFile(imageFile)
                .heatmapFile(heatmapImage)
                .analysisDiagnosis(prediction == 1 ? "Pathology detected" : "Normal")
                .analysisAccuracy(metrics.getAccuracy())
                .analysisPrecision(metrics.getPrecision())
                .analysisRecall(metrics.getRecall())
                .creationDatetime(LocalDateTime.now())
                .patient(patient)
                .doctor(doctor)
                .build();
        return imageAnalysisRepository.save(analysis);
    }

    public Optional<ImageAnalysis> getAnalysis(Long id) {
        return imageAnalysisRepository.findById(id);
    }

    public Optional<byte[]> getHeatmapBytes(Long id) throws IOException {
        return imageAnalysisRepository.findById(id).map(a -> {
            try {
                File file = new File(a.getHeatmapFile().getImageFileUrl());
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Cannot read heatmap file", e);
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