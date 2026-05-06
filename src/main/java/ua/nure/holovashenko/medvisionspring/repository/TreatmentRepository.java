package ua.nure.holovashenko.medvisionspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nure.holovashenko.medvisionspring.entity.Hospital;
import ua.nure.holovashenko.medvisionspring.entity.Treatment;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.TreatmentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findAllByHospital(Hospital hospital);

    List<Treatment> findAllByHospitalAndPatient(Hospital hospital, User patient);

    List<Treatment> findAllByHospitalAndPrimaryDoctor(Hospital hospital, User doctor);

    List<Treatment> findAllByHospitalAndStatus(Hospital hospital, TreatmentStatus status);

    Optional<Treatment> findByHospitalAndPatientAndPrimaryDoctorAndStatus(
            Hospital hospital,
            User patient,
            User primaryDoctor,
            TreatmentStatus status
    );

    Optional<Treatment> findByTreatmentIdAndHospital(Long treatmentId, Hospital hospital);
}
