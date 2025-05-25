package ua.nure.holovashenko.medvisionspring.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.holovashenko.medvisionspring.entity.DiagnosisHistory;

import java.util.List;

public interface DiagnosisHistoryRepository extends JpaRepository<DiagnosisHistory, Long> {
    List<DiagnosisHistory> findByImageAnalysis_ImageAnalysisIdOrderByChangeDatetimeDesc(Long imageAnalysisId);
}