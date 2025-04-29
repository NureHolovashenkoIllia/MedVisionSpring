package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;

@Repository
public interface ImageAnalysisRepository extends JpaRepository<ImageAnalysis, Long> {
}
