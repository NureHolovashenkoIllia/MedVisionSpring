package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.AddNoteRequest;
import ua.nure.holovashenko.medvisionspring.dto.AnalysisNoteResponse;
import ua.nure.holovashenko.medvisionspring.entity.AnalysisNote;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.AnalysisNoteRepository;
import ua.nure.holovashenko.medvisionspring.repository.ImageAnalysisRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisNoteService {

    private final AnalysisNoteRepository noteRepository;
    private final ImageAnalysisRepository imageAnalysisRepository;

    public List<AnalysisNoteResponse> getNotesByAnalysisId(Long analysisId) {
        ImageAnalysis imageAnalysis = imageAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ApiException("Аналіз не знайдено", HttpStatus.NOT_FOUND));
        return noteRepository.findByImageAnalysis(imageAnalysis).stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<AnalysisNoteResponse> getNoteById(Long id) {
        return noteRepository.findById(id).map(this::mapToDto);
    }

    public boolean updateNote(AddNoteRequest request, Long noteId) {
        return noteRepository.findById(noteId).map(note -> {
            note.setNoteText(request.getNoteText());
            note.setNoteAreaX(request.getNoteAreaX());
            note.setNoteAreaY(request.getNoteAreaY());
            note.setNoteAreaWidth(request.getNoteAreaWidth());
            note.setNoteAreaHeight(request.getNoteAreaHeight());

            noteRepository.save(note);
            return true;
        }).orElse(false);
    }

    public boolean deleteNote(Long id) {
        if (!noteRepository.existsById(id)) {
            return false;
        }
        noteRepository.deleteById(id);
        return true;
    }

    private AnalysisNoteResponse mapToDto(AnalysisNote note) {
        AnalysisNoteResponse dto = new AnalysisNoteResponse();
        dto.setAnalysisNoteId(note.getAnalysisNoteId());
        dto.setNoteText(note.getNoteText());
        dto.setNoteAreaX(note.getNoteAreaX());
        dto.setNoteAreaY(note.getNoteAreaY());
        dto.setNoteAreaWidth(note.getNoteAreaWidth());
        dto.setNoteAreaHeight(note.getNoteAreaHeight());
        dto.setCreationDatetime(note.getCreationDatetime());

        if (note.getImageAnalysis() != null) {
            dto.setImageAnalysisId(note.getImageAnalysis().getImageAnalysisId());

            if (note.getImageAnalysis().getImageFile() != null) {
                dto.setImageFileId(note.getImageAnalysis().getImageFile().getImageFileId());
            }
            if (note.getImageAnalysis().getHeatmapFile() != null) {
                dto.setHeatmapFileId(note.getImageAnalysis().getHeatmapFile().getImageFileId());
            }
        }

        if (note.getDoctor() != null) {
            dto.setDoctorId(note.getDoctor().getDoctorId());
        }

        return dto;
    }
}