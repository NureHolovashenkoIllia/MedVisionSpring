package ua.nure.holovashenko.medvisionspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.holovashenko.medvisionspring.dto.*;
import ua.nure.holovashenko.medvisionspring.entity.Doctor;
import ua.nure.holovashenko.medvisionspring.entity.Patient;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.enums.Gender;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.DoctorRepository;
import ua.nure.holovashenko.medvisionspring.repository.PatientRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;
import ua.nure.holovashenko.medvisionspring.security.JwtService;
import ua.nure.holovashenko.medvisionspring.storage.BlobStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BlobStorageService blobStorageService;

    public AuthResponse registerPatient(PatientRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Користувач з таким email вже існує", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUserName(request.getName());
        user.setEmail(request.getEmail());
        user.setPw(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(UserRole.PATIENT);

        User savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(Enum.valueOf(Gender.class, request.getGender().toUpperCase()));
        patientRepository.save(patient);

        String jwt = jwtService.generateToken(savedUser);
        return new AuthResponse(jwt);
    }

    public AuthResponse registerAdmin(AdminRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Користувач з таким email вже існує", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUserName(request.getName());
        user.setEmail(request.getEmail());
        user.setPw(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(UserRole.ADMIN);

        User savedUser = userRepository.save(user);

        String jwt = jwtService.generateToken(savedUser);
        return new AuthResponse(jwt);
    }


    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ApiException("Невірна електронна пошта або пароль", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Користувач не знайдений", HttpStatus.NOT_FOUND));

        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt);
    }

    public UserProfileResponse getProfile(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));

        return switch (user.getUserRole()) {
            case DOCTOR -> {
                Doctor doctor = doctorRepository.findByUser(user)
                        .orElseThrow(() -> new ApiException("Дані лікаря не знайдені", HttpStatus.NOT_FOUND));
                yield DoctorProfileResponse.builder()
                        .id(user.getUserId())
                        .name(user.getUserName())
                        .email(user.getEmail())
                        .role(user.getUserRole())
                        .position(doctor.getPosition())
                        .department(doctor.getDepartment())
                        .licenseNumber(doctor.getLicenseNumber())
                        .achievements(doctor.getAchievements())
                        .medicalInstitution(doctor.getMedicalInstitution())
                        .education(doctor.getEducation())
                        .build();
            }
            case PATIENT -> {
                Patient patient = patientRepository.findByUser(user)
                        .orElseThrow(() -> new ApiException("Дані пацієнта не знайдені", HttpStatus.NOT_FOUND));
                yield PatientProfileResponse.builder()
                        .id(user.getUserId())
                        .name(user.getUserName())
                        .email(user.getEmail())
                        .role(user.getUserRole())
                        .birthDate(patient.getBirthDate())
                        .gender(patient.getGender().name())
                        .heightCm(patient.getHeightCm())
                        .weightKg(patient.getWeightKg())
                        .chronicDiseases(patient.getChronicDiseases())
                        .allergies(patient.getAllergies())
                        .address(patient.getAddress())
                        .lastExamDate(patient.getLastExamDate())
                        .build();
            }
            case ADMIN -> {
                yield AdminProfileResponse.builder()
                        .id(user.getUserId())
                        .name(user.getUserName())
                        .email(user.getEmail())
                        .role(user.getUserRole())
                        .build();
            }
            default -> throw new ApiException("Невідома роль", HttpStatus.BAD_REQUEST);
        };
    }

    @Transactional
    public UserProfileResponse editDoctorProfile(UserDetails userDetails, DoctorEditRequest request) {
        User user = getUser(userDetails);
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new ApiException("Дані лікаря не знайдені", HttpStatus.NOT_FOUND));

        updateUser(user, request.getName());
        doctor.setPosition(request.getPosition());
        doctor.setDepartment(request.getDepartment());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setAchievements(request.getAchievements());
        doctor.setEducation(request.getEducation());
        doctor.setMedicalInstitution(request.getMedicalInstitution());

        return getProfile(userDetails);
    }

    @Transactional
    public UserProfileResponse editPatientProfile(UserDetails userDetails, PatientEditRequest request) {
        User user = getUser(userDetails);
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ApiException("Дані пацієнта не знайдені", HttpStatus.NOT_FOUND));

        updateUser(user, request.getName());
        patient.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        patient.setBirthDate(request.getBirthDate());
        patient.setHeightCm(request.getHeightCm());
        patient.setWeightKg(request.getWeightKg());
        patient.setChronicDiseases(request.getChronicDiseases());
        patient.setAllergies(request.getAllergies());
        patient.setAddress(request.getAddress());

        return getProfile(userDetails);
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException("Користувача не знайдено", HttpStatus.NOT_FOUND));
    }

    private void updateUser(User user, String name) {
        user.setUserName(name);
    }

    @Transactional
    public String uploadAvatar(UserDetails userDetails, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException("Файл не може бути порожнім", HttpStatus.BAD_REQUEST);
        }

        User user = getUser(userDetails);

        String oldAvatarUrl = user.getAvatarUrl();
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
            try {
                Path oldPath = Paths.get(oldAvatarUrl);
                Path storageDirPath = Paths.get("local_images").toAbsolutePath();
                String oldBlobName = storageDirPath.relativize(oldPath).toString().replace("\\", "/");
                blobStorageService.deleteFile(oldBlobName);
            } catch (IOException e) {
                log.warn("Не вдалося видалити старий аватар: {}", e.getMessage());
            }
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : "";
        String blobName = "avatars/" + UUID.randomUUID() + extension;

        try {
            byte[] data = file.getBytes();
            String storedPath = blobStorageService.uploadFileFromBytes(data, blobName, file.getContentType());

            user.setAvatarUrl(storedPath);
            userRepository.save(user);

            return storedPath;
        } catch (IOException e) {
            throw new ApiException("Помилка збереження файлу: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<byte[]> getUserAvatar(UserDetails userDetails) {
        User user = getUser(userDetails);

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            throw new ApiException("Аватар не знайдено", HttpStatus.NOT_FOUND);
        }

        try {
            Path avatarPath = new File(user.getAvatarUrl()).toPath();

            String contentType = Files.probeContentType(avatarPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            byte[] data = Files.readAllBytes(avatarPath);

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(data);
        } catch (IOException e) {
            throw new ApiException("Не вдалося завантажити аватар: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
