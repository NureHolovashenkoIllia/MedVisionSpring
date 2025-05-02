package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.ImageAnalysisResponse;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
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


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable Long id) {
        boolean deleted = analysisService.deleteAnalysis(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
