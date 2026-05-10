package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.holovashenko.medvisionspring.dto.AnalysisJobResponse;
import ua.nure.holovashenko.medvisionspring.entity.*;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisJobStatus;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.AnalysisJobRepository;
import ua.nure.holovashenko.medvisionspring.repository.HospitalRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalysisJobService {

    private final AnalysisJobRepository analysisJobRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final HospitalAccessPolicy accessPolicy;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnalysisJob createProcessingJob(Hospital hospital, Treatment treatment, User requestedBy, String requestPayload) {
        return analysisJobRepository.save(AnalysisJob.builder()
                .hospital(hospital)
                .treatment(treatment)
                .requestedBy(requestedBy)
                .status(AnalysisJobStatus.PROCESSING)
                .requestPayload(requestPayload)
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public AnalysisJob completeJob(AnalysisJob job, ImageAnalysis analysis, String responsePayload) {
        AnalysisJob managedJob = getJobEntity(job);
        managedJob.setImageAnalysis(analysis);
        managedJob.setStatus(AnalysisJobStatus.COMPLETED);
        managedJob.setResponsePayload(responsePayload);
        managedJob.setFinishedAt(LocalDateTime.now());
        return analysisJobRepository.save(managedJob);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnalysisJob failJob(AnalysisJob job, String errorMessage) {
        AnalysisJob managedJob = getJobEntity(job);
        managedJob.setImageAnalysis(null);
        managedJob.setStatus(AnalysisJobStatus.FAILED);
        managedJob.setErrorMessage(errorMessage);
        managedJob.setFinishedAt(LocalDateTime.now());
        return analysisJobRepository.save(managedJob);
    }

    public AnalysisJobResponse getJob(Long hospitalId, Long jobId, UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ApiException("Лікарню не знайдено", HttpStatus.NOT_FOUND));
        accessPolicy.requireHospitalAccess(currentUser, hospital);

        AnalysisJob job = analysisJobRepository.findByAnalysisJobIdAndHospital(jobId, hospital)
                .orElseThrow(() -> new ApiException("Job аналізу не знайдено", HttpStatus.NOT_FOUND));

        return mapToResponse(job);
    }

    public AnalysisJobResponse mapToResponse(AnalysisJob job) {
        return AnalysisJobResponse.builder()
                .jobId(job.getAnalysisJobId())
                .analysisId(job.getImageAnalysis() != null ? job.getImageAnalysis().getImageAnalysisId() : null)
                .hospitalId(job.getHospital() != null ? job.getHospital().getHospitalId() : null)
                .treatmentId(job.getTreatment() != null ? job.getTreatment().getTreatmentId() : null)
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .build();
    }

    private AnalysisJob getJobEntity(AnalysisJob job) {
        if (job == null || job.getAnalysisJobId() == null) {
            throw new ApiException("Job аналізу не знайдено", HttpStatus.NOT_FOUND);
        }
        return analysisJobRepository.findById(job.getAnalysisJobId())
                .orElseThrow(() -> new ApiException("Job аналізу не знайдено", HttpStatus.NOT_FOUND));
    }
}
