package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.AnalysisCreateResponse;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.service.DoctorAnalysisService;

import java.io.IOException;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/treatments/{treatmentId}/analyses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class TreatmentAnalysisController {

    private final DoctorAnalysisService doctorAnalysisService;

    @PostMapping
    public ResponseEntity<AnalysisCreateResponse> uploadAndAnalyzeImage(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        ImageAnalysis analysis = doctorAnalysisService.analyzeAndSave(file, hospitalId, treatmentId, userDetails);

        return ResponseEntity.ok(AnalysisCreateResponse.builder()
                .analysisId(analysis.getImageAnalysisId())
                .jobId(analysis.getAnalysisJob() != null ? analysis.getAnalysisJob().getAnalysisJobId() : null)
                .hospitalId(analysis.getHospital() != null ? analysis.getHospital().getHospitalId() : null)
                .treatmentId(analysis.getTreatment() != null ? analysis.getTreatment().getTreatmentId() : null)
                .status(analysis.getAnalysisStatus())
                .diagnosisClass(analysis.getDiagnosisClass())
                .build());
    }
}
