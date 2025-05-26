package ua.nure.holovashenko.medvisionspring.svm;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

@Service
public class HeatmapGenerator {

    /**
     * Створює теплову карту з матриці значень.
     *
     * @param inputImage   вихідне зображення (може бути null, якщо хочеш тільки теплову карту)
     * @param heatmapData  матриця ймовірностей або значень [0..1]
     * @return зображення з накладеною тепловою картою
     */
    public Mat generateHeatmap(Mat inputImage, double[][] heatmapData) {
        int rows = heatmapData.length;
        int cols = heatmapData[0].length;

        // Створити float-матрицю для теплової карти
        Mat heatmapMat = new Mat(rows, cols, opencv_core.CV_32F);
        FloatIndexer indexer = heatmapMat.createIndexer();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                indexer.put(y, x, (float) heatmapData[y][x]);
            }
        }

        // Нормалізація [0–255]
        opencv_core.normalize(heatmapMat, heatmapMat, 0, 255, opencv_core.NORM_MINMAX, opencv_core.CV_8U, null);

        // Застосування кольорової карти
        Mat colorMap = new Mat();
        opencv_imgproc.applyColorMap(heatmapMat, colorMap, opencv_imgproc.COLORMAP_JET);

        if (inputImage != null && !inputImage.empty()) {
            // Масштабування heatmap до розміру зображення
            Mat resizedHeatmap = new Mat();
            opencv_imgproc.resize(colorMap, resizedHeatmap, inputImage.size());

            // Накладання з alpha
            double alpha = 0.5;
            Mat blended = new Mat();
            opencv_core.addWeighted(inputImage, 1.0 - alpha, resizedHeatmap, alpha, 0.0, blended);
            return blended;
        } else {
            return colorMap;
        }
    }

    /**
     * Зберігає зображення теплової карти у файл.
     */
    public void saveHeatmap(Mat heatmap, String outputPath) {
        opencv_imgcodecs.imwrite(outputPath, heatmap);
    }
}