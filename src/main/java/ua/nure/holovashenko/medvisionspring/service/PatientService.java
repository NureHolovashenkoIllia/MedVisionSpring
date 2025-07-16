package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;
import ua.nure.holovashenko.medvisionspring.entity.User;
import ua.nure.holovashenko.medvisionspring.repository.ImageAnalysisRepository;
import ua.nure.holovashenko.medvisionspring.repository.UserRepository;
import ua.nure.holovashenko.medvisionspring.storage.ResilientBlobStorageService;
import ua.nure.holovashenko.medvisionspring.util.pdf.PdfAnalysisReportGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final ImageAnalysisRepository imageAnalysisRepository;
    private final UserRepository userRepository;
    private final ResilientBlobStorageService blobStorageService;

    public List<ImageAnalysis> getAnalyses(UserDetails userDetails) {
        User patient = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пацієнта не знайдено"));
        return imageAnalysisRepository.findAllByPatient(patient);
    }

    public List<ImageAnalysis> getUnviewedAnalyses(UserDetails userDetails) {
        User patient = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пацієнта не знайдено"));
        return imageAnalysisRepository.findAllByPatientAndViewedFalse(patient);
    }


    public Optional<ImageAnalysis> getAnalysisById(Long id, UserDetails userDetails) {
        Optional<ImageAnalysis> analysisOpt = imageAnalysisRepository.findById(id)
                .filter(a -> a.getPatient().getEmail().equals(userDetails.getUsername()));

        analysisOpt.ifPresent(analysis -> {
            if (!analysis.isViewed()) {
                analysis.setViewed(true);
                imageAnalysisRepository.save(analysis);
            }
        });

        return analysisOpt;
    }

    public void markAnalysisAsViewed(Long id, UserDetails userDetails) {
        ImageAnalysis analysis = imageAnalysisRepository.findById(id)
                .filter(a -> a.getPatient().getEmail().equals(userDetails.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Аналіз не знайдено"));

        if (!analysis.isViewed()) {
            analysis.setViewed(true);
            imageAnalysisRepository.save(analysis);
        }
    }

    public Optional<byte[]> getHeatmapBytes(Long id, UserDetails userDetails) throws IOException {
        return getAnalysisById(id, userDetails).map(a -> {
            try {
                return blobStorageService.downloadFileFromBlobUrl(a.getHeatmapFile().getImageFileUrl());
            } catch (IOException e) {
                throw new RuntimeException("Cannot read heatmap from Azure Blob Storage", e);
            }
        });
    }

    public byte[] exportAnalysisToPdf(Long id, UserDetails userDetails) throws IOException {
        ImageAnalysis analysis = getAnalysisById(id, userDetails)
                .orElseThrow(() -> new IllegalArgumentException("Аналіз не знайдено або немає доступу"));

        return PdfAnalysisReportGenerator.generateAnalysisPdf(analysis);
    }
}
