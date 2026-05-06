package ua.nure.holovashenko.medvisionspring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.HospitalCreateRequest;
import ua.nure.holovashenko.medvisionspring.dto.HospitalResponse;
import ua.nure.holovashenko.medvisionspring.dto.HospitalUpdateRequest;
import ua.nure.holovashenko.medvisionspring.service.HospitalService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hospitals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HospitalAdminController {

    private final HospitalService hospitalService;

    @PostMapping
    public ResponseEntity<HospitalResponse> createHospital(@Valid @RequestBody HospitalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hospitalService.createHospital(request));
    }

    @GetMapping
    public ResponseEntity<List<HospitalResponse>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @PatchMapping("/{hospitalId}")
    public ResponseEntity<HospitalResponse> updateHospital(
            @PathVariable Long hospitalId,
            @Valid @RequestBody HospitalUpdateRequest request
    ) {
        return ResponseEntity.ok(hospitalService.updateHospital(hospitalId, request));
    }
}
