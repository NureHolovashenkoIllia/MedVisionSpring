package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.SvmModelMetricResponse;
import ua.nure.holovashenko.medvisionspring.dto.SvmModelVersionResponse;
import ua.nure.holovashenko.medvisionspring.enums.DatasetSplit;
import ua.nure.holovashenko.medvisionspring.service.SvmModelRegistryService;

import java.util.List;

@RestController
@RequestMapping("/api/svm/models")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SvmModelController {

    private final SvmModelRegistryService registryService;

    @GetMapping
    public ResponseEntity<List<SvmModelVersionResponse>> getModels() {
        return ResponseEntity.ok(registryService.getModels());
    }

    @GetMapping("/{modelVersionId}/metrics")
    public ResponseEntity<List<SvmModelMetricResponse>> getMetrics(
            @PathVariable Long modelVersionId,
            @RequestParam(required = false) DatasetSplit split
    ) {
        return ResponseEntity.ok(registryService.getMetrics(modelVersionId, split));
    }
}
