package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageAnalysisRepository extends JpaRepository<ImageAnalysis, Long> {
    List<ImageAnalysis> findAllByPatient(User patient);

    List<ImageAnalysis> findAllByDoctor(User doctor);

    List<ImageAnalysis> findAllByPatientAndViewedFalse(User patient);
}
