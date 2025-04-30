package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.entity.Doctor;
import ua.nure.holovashenko.medvisionspring.entity.Patient;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.DoctorRepository;
import ua.nure.holovashenko.medvisionspring.repository.PatientRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    public void changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));

        UserRole oldRole = user.getUserRole();

        // Видаляємо старий запис, якщо був DOCTOR або PATIENT
        if (oldRole == UserRole.DOCTOR) {
            doctorRepository.findByUser(user).ifPresent(doctorRepository::delete);
        } else if (oldRole == UserRole.PATIENT) {
            patientRepository.findByUser(user).ifPresent(patientRepository::delete);
        }

        // Оновлюємо роль
        user.setUserRole(newRole);
        userRepository.save(user);

        // Додаємо запис у відповідну таблицю
        if (newRole == UserRole.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctorRepository.save(doctor);
        } else if (newRole == UserRole.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(user);
            patientRepository.save(patient);
        }
    }
}
