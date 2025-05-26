package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.ComparisonReport;
import ua.nure.holovashenko.medvisionspring.dto.ImageAnalysisResponse;
import ua.nure.holovashenko.medvisionspring.dto.UpdateStatusRequest;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;
import ua.nure.holovashenko.medvisionspring.service.AnalysisService;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    public List<ImageAnalysisResponse> getAllAnalyses() {
        return analysisService.getAllAnalyses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageAnalysisResponse> getAnalysis(@PathVariable Long id) {
        return analysisService.getAnalysisById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doctor/{doctorId}")
    public List<ImageAnalysisResponse> getAnalysesByDoctor(@PathVariable Long doctorId) {
        return analysisService.getAnalysesByDoctorId(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<ImageAnalysisResponse> getAnalysesByPatient(@PathVariable Long patientId) {
        return analysisService.getAnalysesByPatientId(patientId);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateAnalysisStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        analysisService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<AnalysisStatus> getAnalysisStatus(@PathVariable Long id) {
        AnalysisStatus status = analysisService.getStatusById(id);
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    @GetMapping("/compare")
    public ResponseEntity<ComparisonReport> compareAnalyses(
            @RequestParam Long fromId,
            @RequestParam Long toId) {
        ComparisonReport report = analysisService.compareAnalyses(fromId, toId);
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    @GetMapping("/compare/pdf")
    public ResponseEntity<byte[]> downloadComparisonPdf(
            @RequestParam Long fromId,
            @RequestParam Long toId) {
        ComparisonReport report = analysisService.compareAnalyses(fromId, toId);
        byte[] pdfBytes = analysisService.exportComparisonToPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("comparison_" + fromId + "_vs_" + toId + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable Long id) {
        boolean deleted = analysisService.deleteAnalysis(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
