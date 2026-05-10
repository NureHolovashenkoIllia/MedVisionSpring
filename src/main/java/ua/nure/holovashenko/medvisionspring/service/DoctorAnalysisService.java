package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.dto.ImageAnalysisResponse;
import ua.nure.holovashenko.medvisionspring.dto.PatientProfileResponse;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final TransactionTemplate transactionTemplate;
    private final TreatmentRepository treatmentRepository;
    private final AnalysisService analysisService;

    public ImageAnalysis analyzeAndSave(MultipartFile file, Long patientId, Long doctorId) throws IOException {
        User patientUser = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Пацієнт не знайдений", HttpStatus.NOT_FOUND));
        User doctorUser = userRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException("Лікар не знайдений", HttpStatus.NOT_FOUND));

        Treatment treatment = legacyTreatmentResolver.resolve(patientUser, doctorUser);
        return analyzeAndSave(file, treatment, doctorUser);
    }

    public ImageAnalysis analyzeAndSave(MultipartFile file, Long hospitalId, Long treatmentId, UserDetails userDetails) throws IOException {
        User doctorUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Лікар не знайдений", HttpStatus.NOT_FOUND));
        Treatment treatment = treatmentService.getTreatmentForAnalysis(hospitalId, treatmentId, userDetails);
        return analyzeAndSave(file, treatment, doctorUser);
    }

    private ImageAnalysis analyzeAndSave(MultipartFile file, Treatment treatment, User doctorUser) throws IOException {
        String contentType = resolveContentType(file);
        String safeFileName = resolveSafeFileName(file);
        AnalysisJob job = analysisJobService.createProcessingJob(
                treatment.getHospital(),
                treatment,
                doctorUser,
                "fileName=" + safeFileName + ";contentType=" + contentType
        );
        File tempFile = File.createTempFile("upload-", ".png");
        File heatmapFile = null;
        try {
            file.transferTo(tempFile);

            SvmClassificationResult classification = svmClient.classify(new SvmClassificationRequest(tempFile, true));
            SvmModelVersion modelVersion = svmModelRegistryService.getActiveFullImageModel();

            String imageObjectName = "images/upload-" + System.currentTimeMillis() + "-" + safeFileName;
            String imageUrl = blobStorageService.uploadFile(tempFile, imageObjectName, contentType);

            heatmapFile = File.createTempFile("heatmap-", ".png");
            imageUtils.saveMatToFile(classification.heatmap(), heatmapFile);

            String heatmapObjectName = "heatmaps/heatmap-" + System.currentTimeMillis() + "-" + safeFileName;
            String heatmapUrl = blobStorageService.uploadFile(heatmapFile, heatmapObjectName, contentType);

            return saveCompletedAnalysis(
                    treatment.getTreatmentId(),
                    doctorUser.getUserId(),
                    job,
                    classification,
                    modelVersion,
                    imageObjectName,
                    imageUrl,
                    heatmapObjectName,
                    heatmapUrl,
                    contentType
            );
        } catch (Exception e) {
            try {
                analysisJobService.failJob(job, e.getMessage());
            } catch (Exception jobFailureException) {
                log.error("Failed to mark analysis job {} as FAILED", job.getAnalysisJobId(), jobFailureException);
            }
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        } finally {
            tempFile.delete();
            if (heatmapFile != null) {
                heatmapFile.delete();
            }
        }
    }

    private ImageAnalysis saveCompletedAnalysis(
            Long treatmentId,
            Long doctorUserId,
            AnalysisJob job,
            SvmClassificationResult classification,
            SvmModelVersion modelVersion,
            String imageObjectName,
            String imageUrl,
            String heatmapObjectName,
            String heatmapUrl,
            String contentType
    ) {
        return transactionTemplate.execute(status -> {
            Treatment managedTreatment = treatmentRepository.findById(treatmentId)
                    .orElseThrow(() -> new ApiException("Лікування не знайдено", HttpStatus.NOT_FOUND));
            User managedDoctorUser = userRepository.findById(doctorUserId)
                    .orElseThrow(() -> new ApiException("Лікар не знайдений", HttpStatus.NOT_FOUND));
            User patientUser = managedTreatment.getPatient();
            Hospital hospital = managedTreatment.getHospital();

            ImageFile imageFile = ImageFile.builder()
                    .imageFileName(imageObjectName)
                    .imageFileType(contentType)
                    .uploadedAt(LocalDateTime.now())
                    .imageFileUrl(imageUrl)
                    .uploadedBy(managedDoctorUser)
                    .hospital(hospital)
                    .treatment(managedTreatment)
                    .build();
            imageFileRepository.save(imageFile);

            ImageFile heatmapImage = ImageFile.builder()
                    .imageFileName(heatmapObjectName)
                    .imageFileType(contentType)
                    .uploadedAt(LocalDateTime.now())
                    .imageFileUrl(heatmapUrl)
                    .uploadedBy(managedDoctorUser)
                    .hospital(hospital)
                    .treatment(managedTreatment)
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
                    .doctor(managedDoctorUser)
                    .hospital(hospital)
                    .treatment(managedTreatment)
                    .analysisJob(job)
                    .modelVersion(modelVersion)
                    .build();

            ImageAnalysis savedAnalysis = imageAnalysisRepository.save(analysis);

            Doctor doctor = doctorRepository.findById(managedDoctorUser.getUserId()).orElse(null);

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
        });
    }

    private String resolveContentType(MultipartFile file) {
        return file.getContentType() != null && !file.getContentType().isBlank()
                ? file.getContentType()
                : "application/octet-stream";
    }

    private String resolveSafeFileName(MultipartFile file) {
        String original = Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> !name.isBlank())
                .orElse("scan.png");
        String baseName = original.replace('\\', '/');
        int slashIndex = baseName.lastIndexOf('/');
        if (slashIndex >= 0) {
            baseName = baseName.substring(slashIndex + 1);
        }
        baseName = baseName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (baseName.isBlank()) {
            baseName = "scan.png";
        }
        if (baseName.length() > 80) {
            int dotIndex = baseName.lastIndexOf('.');
            String extension = dotIndex > 0 && baseName.length() - dotIndex <= 12
                    ? baseName.substring(dotIndex)
                    : "";
            int maxBaseLength = 80 - extension.length();
            baseName = baseName.substring(0, Math.max(1, maxBaseLength)) + extension;
        }
        return UUID.randomUUID() + "-" + baseName;
    }

    public Optional<ImageAnalysis> getAnalysis(Long id) {
        return imageAnalysisRepository.findById(id);
    }

    public Optional<ImageAnalysisResponse> getAnalysisResponse(Long id) {
        return imageAnalysisRepository.findById(id).map(analysisService::mapToDto);
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

    public List<PatientProfileResponse> getAllPatientProfiles() {
        return patientRepository.findAll().stream()
                .map(this::mapPatientProfile)
                .toList();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    public PatientProfileResponse getPatientProfileById(Long id) {
        return patientRepository.findById(id)
                .map(this::mapPatientProfile)
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    private PatientProfileResponse mapPatientProfile(Patient patient) {
        User user = patient.getUser();
        return PatientProfileResponse.builder()
                .id(patient.getPatientId())
                .name(user != null ? user.getUserName() : null)
                .email(user != null ? user.getEmail() : null)
                .role(user != null ? user.getUserRole() : null)
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender() != null ? patient.getGender().name() : null)
                .heightCm(patient.getHeightCm())
                .weightKg(patient.getWeightKg())
                .chronicDiseases(patient.getChronicDiseases())
                .allergies(patient.getAllergies())
                .address(patient.getAddress())
                .lastExamDate(patient.getLastExamDate())
                .hospitals(List.of())
                .build();
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
