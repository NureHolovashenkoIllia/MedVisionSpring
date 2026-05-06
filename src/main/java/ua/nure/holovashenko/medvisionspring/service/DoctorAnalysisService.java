package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.*;
import ua.nure.holovashenko.medvisionspring.storage.BlobStorageService;
import ua.nure.holovashenko.medvisionspring.svm.ImageUtils;
import ua.nure.holovashenko.medvisionspring.svm.MetricsCalculator;
import ua.nure.holovashenko.medvisionspring.svm.SvmClassificationRequest;
import ua.nure.holovashenko.medvisionspring.svm.SvmClassificationResult;
import ua.nure.holovashenko.medvisionspring.svm.SvmClient;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorAnalysisService {

    private final SvmClient svmClient;
    private final ImageUtils imageUtils;
    private final BlobStorageService blobStorageService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;
    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final ImageFileRepository imageFileRepository;
    private final AnalysisNoteRepository analysisNoteRepository;
    private final LegacyTreatmentResolver legacyTreatmentResolver;
    private final TreatmentService treatmentService;
    private final AnalysisJobService analysisJobService;
    private final SvmModelRegistryService svmModelRegistryService;

    @Transactional
    public ImageAnalysis analyzeAndSave(MultipartFile file, Long patientId, Long doctorId) throws IOException {
        User patientUser = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Пацієнт не знайдений", HttpStatus.NOT_FOUND));
        User doctorUser = userRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException("Лікар не знайдений", HttpStatus.NOT_FOUND));

        Treatment treatment = legacyTreatmentResolver.resolve(patientUser, doctorUser);
        return analyzeAndSave(file, treatment, doctorUser);
    }

    @Transactional
    public ImageAnalysis analyzeAndSave(MultipartFile file, Long hospitalId, Long treatmentId, UserDetails userDetails) throws IOException {
        User doctorUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Лікар не знайдений", HttpStatus.NOT_FOUND));
        Treatment treatment = treatmentService.getTreatmentForAnalysis(hospitalId, treatmentId, userDetails);
        return analyzeAndSave(file, treatment, doctorUser);
    }

    private ImageAnalysis analyzeAndSave(MultipartFile file, Treatment treatment, User doctorUser) throws IOException {
        AnalysisJob job = analysisJobService.createProcessingJob(
                treatment.getHospital(),
                treatment,
                doctorUser,
                "fileName=" + file.getOriginalFilename() + ";contentType=" + file.getContentType()
        );
        File tempFile = File.createTempFile("upload-", ".png");
        try {
            file.transferTo(tempFile);

            User patientUser = treatment.getPatient();
            SvmClassificationResult classification = svmClient.classify(new SvmClassificationRequest(tempFile, true));
            SvmModelVersion modelVersion = svmModelRegistryService.getActiveFullImageModel();

            String imageObjectName = "images/upload-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            String imageUrl = blobStorageService.uploadFile(tempFile, imageObjectName, file.getContentType());

            ImageFile imageFile = ImageFile.builder()
                    .imageFileName(imageObjectName)
                    .imageFileType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .imageFileUrl(imageUrl)
                    .uploadedBy(doctorUser)
                    .hospital(treatment.getHospital())
                    .treatment(treatment)
                    .build();
            imageFileRepository.save(imageFile);

            File heatmapFile = File.createTempFile("heatmap-", ".png");
            imageUtils.saveMatToFile(classification.heatmap(), heatmapFile);

            String heatmapObjectName = "heatmaps/heatmap-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            String heatmapUrl = blobStorageService.uploadFile(heatmapFile, heatmapObjectName, file.getContentType());

            ImageFile heatmapImage = ImageFile.builder()
                    .imageFileName(heatmapObjectName)
                    .imageFileType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .imageFileUrl(heatmapUrl)
                    .uploadedBy(doctorUser)
                    .hospital(treatment.getHospital())
                    .treatment(treatment)
                    .build();
            imageFileRepository.save(heatmapImage);

            MetricsCalculator.ClassMetrics classMetrics = classification.classMetrics();
            float precision = classMetrics != null ? (float) classMetrics.precision() : 0f;
            float recall = classMetrics != null ? (float) classMetrics.recall() : 0f;

            ImageAnalysis analysis = ImageAnalysis.builder()
                    .imageFile(imageFile)
                    .heatmapFile(heatmapImage)
                    .analysisDetails(classification.diagnosisInfo().getAnalysisDetails())
                    .analysisDiagnosis(classification.diagnosisInfo().getAnalysisDiagnosis())
                    .treatmentRecommendations(classification.diagnosisInfo().getTreatmentRecommendations())
                    .analysisAccuracy((float) classification.metrics().accuracy())
                    .analysisPrecision(precision)
                    .analysisRecall(recall)
                    .creationDatetime(LocalDateTime.now())
                    .analysisStatus(AnalysisStatus.REQUIRES_REVISION)
                    .diagnosisClass(classification.diagnosisClass())
                    .patient(patientUser)
                    .doctor(doctorUser)
                    .hospital(treatment.getHospital())
                    .treatment(treatment)
                    .analysisJob(job)
                    .modelVersion(modelVersion)
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

            Patient patient = patientRepository.findById(patientUser.getUserId())
                    .orElseThrow(() -> new ApiException("Пацієнт не знайдений", HttpStatus.NOT_FOUND));

            patient.setLastExamDate(LocalDate.now());
            patientRepository.save(patient);

            analysisJobService.completeJob(
                    job,
                    savedAnalysis,
                    "diagnosisClass=" + savedAnalysis.getDiagnosisClass()
                            + ";analysisId=" + savedAnalysis.getImageAnalysisId()
            );

            return savedAnalysis;
        } catch (Exception e) {
            analysisJobService.failJob(job, e.getMessage());
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        } finally {
            tempFile.delete();
        }
    }

    public Optional<ImageAnalysis> getAnalysis(Long id) {
        return imageAnalysisRepository.findById(id);
    }

    public Optional<byte[]> getHeatmapBytes(Long id) throws IOException {
        return imageAnalysisRepository.findById(id).map(a -> {
            try {
                return blobStorageService.downloadFileFromBlobUrl(a.getHeatmapFile().getImageFileUrl());
            } catch (IOException e) {
                throw new RuntimeException("Cannot read heatmap from Azure Blob Storage", e);
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
