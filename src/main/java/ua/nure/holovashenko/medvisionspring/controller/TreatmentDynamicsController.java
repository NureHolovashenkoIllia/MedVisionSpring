package ua.nure.holovashenko.medvisionspring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.service.TreatmentDynamicsService;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/treatments/{treatmentId}")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TreatmentDynamicsController {

    private final TreatmentDynamicsService treatmentDynamicsService;

    @PostMapping("/comparisons")
    public ResponseEntity<AnalysisComparisonResponse> createComparison(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @Valid @RequestBody AnalysisComparisonCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentDynamicsService.createComparison(hospitalId, treatmentId, request, userDetails));
    }

    @GetMapping("/comparisons")
    public ResponseEntity<List<AnalysisComparisonResponse>> getComparisons(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentDynamicsService.getComparisons(hospitalId, treatmentId, userDetails));
    }

    @PutMapping("/conclusion")
    public ResponseEntity<TreatmentConclusionResponse> upsertConclusion(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @Valid @RequestBody TreatmentConclusionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentDynamicsService.upsertConclusion(hospitalId, treatmentId, request, userDetails));
    }

    @GetMapping("/conclusion")
    public ResponseEntity<TreatmentConclusionResponse> getConclusion(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentDynamicsService.getConclusion(hospitalId, treatmentId, userDetails));
    }

    @GetMapping("/report")
    public ResponseEntity<TreatmentReportResponse> getReport(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentDynamicsService.getReport(hospitalId, treatmentId, userDetails));
    }
}
