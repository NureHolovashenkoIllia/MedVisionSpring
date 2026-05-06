package ua.nure.holovashenko.medvisionspring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.TreatmentCloseRequest;
import ua.nure.holovashenko.medvisionspring.dto.TreatmentCreateRequest;
import ua.nure.holovashenko.medvisionspring.dto.TreatmentResponse;
import ua.nure.holovashenko.medvisionspring.dto.TreatmentTimelineResponse;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;
import ua.nure.holovashenko.medvisionspring.service.TreatmentService;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/treatments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TreatmentController {

    private final TreatmentService treatmentService;

    @PostMapping
    public ResponseEntity<TreatmentResponse> createTreatment(
            @PathVariable Long hospitalId,
            @Valid @RequestBody TreatmentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(treatmentService.createTreatment(hospitalId, request, userDetails));
    }

    @GetMapping
    public ResponseEntity<List<TreatmentResponse>> getTreatments(
            @PathVariable Long hospitalId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) TreatmentStatus status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentService.getTreatments(hospitalId, patientId, doctorId, status, userDetails));
    }

    @GetMapping("/{treatmentId}")
    public ResponseEntity<TreatmentTimelineResponse> getTreatment(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentService.getTreatment(hospitalId, treatmentId, userDetails));
    }

    @PatchMapping("/{treatmentId}/close")
    public ResponseEntity<TreatmentResponse> closeTreatment(
            @PathVariable Long hospitalId,
            @PathVariable Long treatmentId,
            @Valid @RequestBody TreatmentCloseRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(treatmentService.closeTreatment(hospitalId, treatmentId, request, userDetails));
    }
}
