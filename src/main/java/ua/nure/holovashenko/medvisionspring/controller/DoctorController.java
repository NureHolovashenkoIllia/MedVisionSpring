package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.Patient;
import ua.nure.holovashenko.medvisionspring.service.DoctorAnalysisService;

import java.io.IOException;
import java.util.List;

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

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(doctorAnalysisService.getAllPatients());
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorAnalysisService.getPatientById(id));
    }

    @PostMapping("/analyses/{analysesId}/notes")
    public ResponseEntity<Void> addNote(
            @PathVariable Long analysesId,
            @RequestParam Long doctorId,
            @RequestBody AddNoteRequest noteRequest
    ) {
        boolean added = doctorAnalysisService.addNote(analysesId, doctorId, noteRequest);
        return added ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
