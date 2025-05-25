package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.dto.AnalysisNoteResponse;
import ua.nure.holovashenko.medvisionspring.dto.BoundingBox;
import ua.nure.holovashenko.medvisionspring.service.AnalysisNoteService;

import java.util.List;

@RestController
@RequestMapping("/api/analysis-note")
@RequiredArgsConstructor
public class AnalysisNoteController {

    private final AnalysisNoteService noteService;

    // Отримання всіх заміток до певного аналізу
    @GetMapping("/by-analysis/{analysisId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public List<AnalysisNoteResponse> getNotesByAnalysisId(@PathVariable Long analysisId) {
        return noteService.getNotesByAnalysisId(analysisId);
    }

    @GetMapping("/by-analysis/{analysisId}/bounding-boxes")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<BoundingBox>> getBoundingBoxes(@PathVariable Long analysisId) {
        return ResponseEntity.ok(noteService.getBoundingBoxesByAnalysisId(analysisId));
    }


    // Отримання певної замітки
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AnalysisNoteResponse> getNote(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Змінення інформації про замітку
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Void> updateNote(@PathVariable Long id,
                                           @RequestBody AddNoteRequest request) {
        boolean updated = noteService.updateNote(request, id);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Видалення замітки
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        boolean deleted = noteService.deleteNote(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
