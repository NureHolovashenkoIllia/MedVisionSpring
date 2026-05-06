package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.SvmModelVersion;
import ua.nure.holovashenko.medvisionspring.enums.SvmModelStatus;
import ua.nure.holovashenko.medvisionspring.enums.SvmModelType;

import java.util.List;
import java.util.Optional;

@Repository
public interface SvmModelVersionRepository extends JpaRepository<SvmModelVersion, Long> {
    Optional<SvmModelVersion> findByVersion(String version);

    Optional<SvmModelVersion> findFirstByModelTypeAndStatusOrderByActivatedAtDesc(SvmModelType modelType, SvmModelStatus status);

    List<SvmModelVersion> findAllByStatus(SvmModelStatus status);
}
