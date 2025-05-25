package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.DiagnosisHistoryRequest;
import ua.nure.holovashenko.medvisionspring.dto.DiagnosisHistoryResponse;
import ua.nure.holovashenko.medvisionspring.service.DiagnosisHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisHistoryController {

    private final DiagnosisHistoryService diagnosisHistoryService;

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosisHistoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(diagnosisHistoryService.getById(id));
    }

    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<List<DiagnosisHistoryResponse>> getAllByAnalysis(@PathVariable Long analysisId) {
        return ResponseEntity.ok(diagnosisHistoryService.getAllByAnalysisId(analysisId));
    }

    @PostMapping
    public ResponseEntity<DiagnosisHistoryResponse> createDiagnosis(
            @RequestBody DiagnosisHistoryRequest request) {
        return ResponseEntity.ok(diagnosisHistoryService.createDiagnosis(request));
    }
}
