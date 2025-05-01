package ua.nure.holovashenko.medvisionspring.svm;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

public class ImageProcessor {

    private static final int TARGET_WIDTH = 128;
    private static final int TARGET_HEIGHT = 128;

    /**
     * Завантажує зображення з диска, перетворює у grayscale, масштабує до 128x128 та нормалізує пікселі в [0,1].
     *
     * @param imageFile файл зображення
     * @return нормалізоване зображення типу CV_32F розміром 128x128
     */
    public static Mat preprocessImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("Файл не існує або не задано.");
        }

        // Перевірка MIME-типу
        try {
            String mimeType = Files.probeContentType(imageFile.toPath());
            Set<String> allowedTypes = Set.of("image/png", "image/jpeg", "application/dicom");

            if (mimeType == null || !allowedTypes.contains(mimeType)) {
                throw new IllegalArgumentException("Непідтримуваний тип файлу: " + mimeType);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося визначити тип файлу", e);
        }

        Mat grayscale = opencv_imgcodecs.imread(imageFile.getAbsolutePath(), opencv_imgcodecs.IMREAD_GRAYSCALE);
        if (grayscale.empty()) {
            throw new IllegalArgumentException("Не вдалося завантажити зображення: " + imageFile.getAbsolutePath());
        }

        Mat resized = new Mat();
        opencv_imgproc.resize(grayscale, resized, new Size(TARGET_WIDTH, TARGET_HEIGHT));

        Mat normalized = new Mat();
        resized.convertTo(normalized, opencv_core.CV_32F, 1.0 / 255.0, 0.0);

        return normalized;
    }

    /**
     * Перетворює 2D-зображення у одновимірний вектор (1xN).
     *
     * @param mat зображення
     * @return вектор-рядок
     */
    public static Mat flattenImage(Mat mat) {
        if (mat == null || mat.empty()) {
            throw new IllegalArgumentException("Неможливо перетворити порожню матрицю.");
        }

        if (!mat.isContinuous()) {
            Mat continuous = mat.clone();
            return continuous.reshape(1, 1);
        }

        return mat.reshape(1, 1); // (1 row, all pixels in 1 line)
    }
}
