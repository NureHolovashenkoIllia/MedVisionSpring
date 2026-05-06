package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.enums.HospitalStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByCode(String code);

    boolean existsByCode(String code);

    List<Hospital> findAllByStatus(HospitalStatus status);
}
