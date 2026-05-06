package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.AnalysisComparison;
import ua.nure.holovashenko.medvisionspring.entity.Treatment;

import java.util.List;

@Repository
public interface AnalysisComparisonRepository extends JpaRepository<AnalysisComparison, Long> {
    List<AnalysisComparison> findAllByTreatmentOrderByCreatedAtAsc(Treatment treatment);
}
