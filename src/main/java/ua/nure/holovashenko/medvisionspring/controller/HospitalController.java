package ua.nure.holovashenko.medvisionspring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;
import ua.nure.holovashenko.medvisionspring.service.HospitalMembershipService;
import ua.nure.holovashenko.medvisionspring.service.HospitalService;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class HospitalController {

    private final HospitalService hospitalService;
    private final HospitalMembershipService membershipService;

    @GetMapping("/my")
    public ResponseEntity<List<MyHospitalResponse>> getMyHospitals(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(hospitalService.getMyHospitals(userDetails));
    }

    @PostMapping("/{hospitalId}/select")
    public ResponseEntity<ActiveHospitalResponse> selectHospital(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(hospitalService.selectHospital(hospitalId, userDetails));
    }

    @PostMapping("/{hospitalId}/memberships")
    public ResponseEntity<HospitalMembershipResponse> addMembership(
            @PathVariable Long hospitalId,
            @Valid @RequestBody HospitalMembershipCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(membershipService.addMembership(hospitalId, request, userDetails));
    }

    @GetMapping("/{hospitalId}/memberships")
    public ResponseEntity<List<HospitalMembershipResponse>> getMemberships(
            @PathVariable Long hospitalId,
            @RequestParam(required = false) HospitalRole role,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(membershipService.getMemberships(hospitalId, role, userDetails));
    }

    @PatchMapping("/{hospitalId}/memberships/{membershipId}/status")
    public ResponseEntity<HospitalMembershipResponse> updateMembershipStatus(
            @PathVariable Long hospitalId,
            @PathVariable Long membershipId,
            @Valid @RequestBody HospitalMembershipStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(membershipService.updateMembershipStatus(
                hospitalId,
                membershipId,
                request.getStatus(),
                userDetails
        ));
    }
}
