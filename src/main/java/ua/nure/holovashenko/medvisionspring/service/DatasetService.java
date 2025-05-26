package ua.nure.holovashenko.medvisionspring.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class DatasetService {

    private static final String CSV_PATH = "data/Data_Entry_2017.csv";
    private static final String IMAGES_DIR = "data/images/";
    private static final String OUTPUT_DIR = "data/output_dataset/";

    private static final List<String> CATEGORIES = Arrays.asList("pneumonia", "emphysema", "fibrosis", "healthy", "other", "unknown");

    public String buildDataset() {
        try {
            // 1. Створити вихідні папки
            for (String cat : CATEGORIES) {
                Files.createDirectories(Paths.get(OUTPUT_DIR + cat));
            }

            // 2. Зчитати CSV
            BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH));
            String header = reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;

                String filename = tokens[0].trim();
                String[] labels = tokens[1].trim().split("\\|");

                Path source = Paths.get(IMAGES_DIR + filename);
                if (!Files.exists(source)) {
                    copyToUnknown(filename);
                    continue;
                }

                String targetFolder = getTargetFolder(labels);

                Files.copy(source, Paths.get(OUTPUT_DIR + targetFolder + "/" + filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            reader.close();
            return "Dataset створено в " + OUTPUT_DIR;

        } catch (Exception e) {
            e.printStackTrace();
            return "Помилка при створенні датасету: " + e.getMessage();
        }
    }

    private static String getTargetFolder(String[] labels) {
        Set<String> labelSet = new HashSet<>(Arrays.asList(labels));

        String targetFolder;
        if (labelSet.contains("Pneumonia")) {
            targetFolder = "pneumonia";
        } else if (labelSet.contains("Emphysema")) {
            targetFolder = "emphysema";
        } else if (labelSet.contains("Fibrosis")) {
            targetFolder = "fibrosis";
        } else if (labelSet.size() == 1 && labelSet.contains("No Finding")) {
            targetFolder = "healthy";
        } else {
            targetFolder = "other";
        }
        return targetFolder;
    }

    private void copyToUnknown(String filename) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR + "unknown"));
            Path dest = Paths.get(OUTPUT_DIR + "unknown/" + filename);
            Files.createFile(dest);
        } catch (IOException ignored) {
        }
    }
}