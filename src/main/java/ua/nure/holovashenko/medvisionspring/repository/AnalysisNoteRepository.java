package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.AnalysisNote;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;

import java.util.List;

@Repository
public interface AnalysisNoteRepository extends JpaRepository<AnalysisNote, Long> {
    List<AnalysisNote> findByImageAnalysis(ImageAnalysis imageAnalysis);
}
