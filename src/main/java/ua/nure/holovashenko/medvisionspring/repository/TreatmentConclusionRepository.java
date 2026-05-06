package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.Treatment;
import ua.nure.holovashenko.medvisionspring.entity.TreatmentConclusion;

import java.util.Optional;

@Repository
public interface TreatmentConclusionRepository extends JpaRepository<TreatmentConclusion, Long> {
    Optional<TreatmentConclusion> findByTreatment(Treatment treatment);
}
