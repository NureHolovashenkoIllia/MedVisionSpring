package ua.nure.holovashenko.medvisionspring.svm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DatasetLoader {

    private static final int MAX_IMAGES_PER_CLASS = 200;

    public Dataset loadDataset(String basePath) {
        File baseDir = new File(basePath);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid dataset path: " + basePath);
        }

        Map<String, Integer> labelMap = new HashMap<>();
        List<File> imageFiles = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        File[] classDirs = baseDir.listFiles(File::isDirectory);
        if (classDirs == null) {
            throw new RuntimeException("No class folders found in: " + basePath);
        }

        int labelCounter = 0;

        for (File classDir : classDirs) {
            String className = classDir.getName();
            labelMap.putIfAbsent(className, labelCounter++);
            int label = labelMap.get(className);

            File[] files = classDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg")
                    || name.toLowerCase().endsWith(".png")
                    || name.toLowerCase().endsWith(".jpeg"));

            int addedForClass = 0;

            if (files != null) {
                for (File imgFile : files) {
                    if (addedForClass >= MAX_IMAGES_PER_CLASS) break;

                    imageFiles.add(imgFile);
                    labels.add(label);
                    addedForClass++;
                }
            }

            log.info("Loaded {} images for class '{}'", addedForClass, className);
        }

        log.info("Total loaded images: {}", imageFiles.size());
        log.info("Class mapping: {}", labelMap);

        return new Dataset(imageFiles, labels, labelMap);
    }
}