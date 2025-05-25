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
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;
import ua.nure.holovashenko.medvisionspring.svm.ModelMetrics;
import ua.nure.holovashenko.medvisionspring.svm.SvmService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorAnalysisService {

    private final SvmService svmService;
    private final GcsService gcsService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final ImageFileRepository imageFileRepository;
    private final AnalysisNoteRepository analysisNoteRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ImageAnalysis analyzeAndSave(MultipartFile file, Long patientId, Long doctorId) throws IOException {
        File tempFile = File.createTempFile("upload-", ".png");
        file.transferTo(tempFile);

        User patient = userRepository.findById(patientId).orElseThrow();
        User doctor = userRepository.findById(doctorId).orElseThrow();

        int prediction = svmService.classify(tempFile, false);

        String imageObjectName = "images/upload-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        String imageUrl = gcsService.uploadFile(tempFile, imageObjectName, file.getContentType());

        ImageFile imageFile = ImageFile.builder()
                .imageFileName(imageObjectName)
                .imageFileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .imageFileUrl(imageUrl)
                .uploadedBy(doctor)
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
                .uploadedBy(doctor)
                .build();
        imageFileRepository.save(heatmapImage);

        ModelMetrics metrics = loadMetricsFromGcs("svm-metrics/metrics.json");

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

    public ModelMetrics loadMetricsFromGcs(String metricsPath) {
        try {
            byte[] data = gcsService.downloadFileGcs("gs://medvision458613/" + metricsPath);
            return objectMapper.readValue(data, ModelMetrics.class);
        } catch (IOException e) {
            System.err.println("Не вдалося завантажити метрики з GCS: " + metricsPath);
            e.printStackTrace();
            return new ModelMetrics(0, 0, 0); // або можна повернути null, якщо хочеш обробляти помилки окремо
        }
    }
}
