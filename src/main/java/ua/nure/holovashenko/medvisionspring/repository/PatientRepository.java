package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.Patient;
import ua.nure.holovashenko.medvisionspring.entity.User;

import java.util.Optional;


@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
}
