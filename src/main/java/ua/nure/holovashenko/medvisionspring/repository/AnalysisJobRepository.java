package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.AnalysisJob;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;

import java.util.Optional;

@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {
    Optional<AnalysisJob> findByAnalysisJobIdAndHospital(Long analysisJobId, Hospital hospital);
}
