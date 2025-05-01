package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.service.DoctorAnalysisService;

import java.io.IOException;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorAnalysisService doctorAnalysisService;

    @PostMapping("/images/analyze")
    public ResponseEntity<String> uploadAndAnalyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId") Long patientId,
            @RequestParam("doctorId") Long doctorId
    ) throws IOException {
        ImageAnalysis analysis = doctorAnalysisService.analyzeAndSave(file, patientId, doctorId);
        return ResponseEntity.ok("Analysis saved. ID: " + analysis.getImageAnalysisId());
    }

    @GetMapping("/analysis/{id}")
    public ResponseEntity<ImageAnalysis> getAnalysis(@PathVariable Long id) {
        return doctorAnalysisService.getAnalysis(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/heatmap/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getHeatmapImage(@PathVariable Long id) throws IOException {
        return doctorAnalysisService.getHeatmapBytes(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/analysis/{id}/diagnosis")
    public ResponseEntity<Void> updateDiagnosis(@PathVariable Long id, @RequestBody String diagnosis) {
        boolean updated = doctorAnalysisService.updateDiagnosis(id, diagnosis);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
