package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.service.PatientService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/analyses")
    public ResponseEntity<List<ImageAnalysis>> getMyAnalyses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(patientService.getAnalyses(userDetails));
    }

    @GetMapping("/analyses/{id}")
    public ResponseEntity<ImageAnalysis> getAnalysis(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return patientService.getAnalysisById(id, userDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/heatmap/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getHeatmap(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return patientService.getHeatmapBytes(id, userDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/analyses/pdf/{id}")
    public ResponseEntity<byte[]> exportAnalysisToPdf(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        byte[] pdf = patientService.exportAnalysisToPdf(id, userDetails);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analysis-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
