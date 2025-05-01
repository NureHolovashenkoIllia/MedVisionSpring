package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.Patient;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.repository.ImageAnalysisRepository;
import ua.nure.holovashenko.medvisionspring.repository.PatientRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;
import ua.nure.holovashenko.medvisionspring.util.PdfUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final ImageAnalysisRepository imageAnalysisRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public List<ImageAnalysis> getAnalyses(UserDetails userDetails) {
        User patient = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пацієнта не знайдено"));
        return imageAnalysisRepository.findAllByPatient(patient);
    }

    public Optional<ImageAnalysis> getAnalysisById(Long id, UserDetails userDetails) {
        return imageAnalysisRepository.findById(id)
                .filter(a -> a.getPatient().getEmail().equals(userDetails.getUsername()));
    }

    public Optional<byte[]> getHeatmapBytes(Long id, UserDetails userDetails) throws IOException {
        return getAnalysisById(id, userDetails).map(a -> {
            try {
                File file = new File(a.getHeatmapFile().getImageFileUrl());
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Не вдалося зчитати heatmap", e);
            }
        });
    }

    public byte[] exportAnalysisToPdf(Long id, UserDetails userDetails) throws IOException {
        ImageAnalysis analysis = getAnalysisById(id, userDetails)
                .orElseThrow(() -> new IllegalArgumentException("Аналіз не знайдено або немає доступу"));

        return PdfUtil.generateAnalysisPdf(analysis);
    }
}
