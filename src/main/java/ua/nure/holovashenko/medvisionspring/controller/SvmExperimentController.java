package ua.nure.holovashenko.medvisionspring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.SvmExperimentRequest;
import ua.nure.holovashenko.medvisionspring.dto.SvmExperimentResponse;
import ua.nure.holovashenko.medvisionspring.service.SvmExperimentService;

@RestController
@RequestMapping("/api/svm/experiments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SvmExperimentController {

    private final SvmExperimentService experimentService;

    @PostMapping("/split-preview")
    public ResponseEntity<SvmExperimentResponse> previewSplit(@Valid @RequestBody SvmExperimentRequest request) {
        return ResponseEntity.ok(experimentService.previewSplit(request));
    }
}
