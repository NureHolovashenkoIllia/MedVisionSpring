package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.AnalysisJobResponse;
import ua.nure.holovashenko.medvisionspring.service.AnalysisJobService;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/analysis-jobs")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AnalysisJobController {

    private final AnalysisJobService analysisJobService;

    @GetMapping("/{jobId}")
    public ResponseEntity<AnalysisJobResponse> getJob(
            @PathVariable Long hospitalId,
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(analysisJobService.getJob(hospitalId, jobId, userDetails));
    }
}
