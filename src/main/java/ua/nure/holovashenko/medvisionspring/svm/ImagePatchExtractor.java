package ua.nure.holovashenko.medvisionspring.svm;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ImagePatchExtractor {

    private static final int PATCH_SIZE = 64;
    private static final int STEP_SIZE = 32;

    /**
     * Ділить зображення на патчі розміром PATCH_SIZE з кроком STEP_SIZE,
     * переводить кожен патч у вектор ознак.
     *
     * @param imageFile файл зображення
     * @param label     мітка, яку слід призначити (ігнорується при класифікації)
     * @param convertToFloat чи конвертувати дані в float
     * @return список об'єктів FeatureLabelPair
     */
    public List<FeatureLabelPair> extract(File imageFile, int label, boolean convertToFloat) {
        List<FeatureLabelPair> patchFeatures = new ArrayList<>();

        Mat image = ImageUtils.loadAndResizeImage(imageFile.getAbsolutePath());
        if (image.empty()) {
            log.warn("Не вдалося завантажити зображення: {}", imageFile.getAbsolutePath());
            return patchFeatures;
        }

        int rows = image.rows();
        int cols = image.cols();

        for (int y = 0; y <= rows - PATCH_SIZE; y += STEP_SIZE) {
            for (int x = 0; x <= cols - PATCH_SIZE; x += STEP_SIZE) {
                Mat patchView = new Mat(image, new Rect(x, y, PATCH_SIZE, PATCH_SIZE));
                Mat patch = patchView.clone();
                Mat flatPatch = patch.reshape(1, 1);
                if (convertToFloat) {
                    flatPatch.convertTo(flatPatch, opencv_core.CV_32F);
                }
                patchFeatures.add(new FeatureLabelPair(flatPatch, label));
            }
        }

        return patchFeatures;
    }
}
