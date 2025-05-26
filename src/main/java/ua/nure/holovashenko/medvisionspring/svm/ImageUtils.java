package ua.nure.holovashenko.medvisionspring.svm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_core.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

@Component
public class ImageUtils {

    public static final int TARGET_WIDTH = 256;
    public static final int TARGET_HEIGHT = 256;

    public static Mat loadAndResizeImage(String imagePath) {
        // Завантаження зображення у відтінках сірого
        Mat image = opencv_imgcodecs.imread(imagePath, opencv_imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            throw new IllegalArgumentException("Cannot read image: " + imagePath);
        }

        // Зміна розміру до 256x256
        Mat resizedImage = new Mat();
        opencv_imgproc.resize(image, resizedImage, new Size(TARGET_WIDTH, TARGET_HEIGHT));

        return resizedImage;
    }

    public static Mat loadImage(String imageUrl) {
        try {
            URL url = URI.create(imageUrl).toURL();
            URLConnection connection = url.openConnection();

            String contentType = connection.getContentType();
            if (!contentType.startsWith("image/")) {
                throw new IOException("Не зображення: " + contentType);
            }

            try (InputStream in = connection.getInputStream()) {
                byte[] bytes = in.readAllBytes();

                // Конвертуємо в BytePointer
                BytePointer bytePointer = new BytePointer(bytes);

                // Декодуємо
                Mat image = opencv_imgcodecs.imdecode(new Mat(bytePointer), opencv_imgcodecs.IMREAD_COLOR);

                if (image.empty()) {
                    throw new IOException("Не вдалося розпізнати зображення: " + imageUrl);
                }

                return image;
            }
        } catch (Exception e) {
            throw new RuntimeException("Помилка при завантаженні зображення: " + imageUrl, e);
        }
    }

    /**
     * Зберігає зображення у файл.
     */
    public void saveImage(Mat image, String outputPath) {
        opencv_imgcodecs.imwrite(outputPath, image);
    }

    /**
     * Змінює розмір зображення до вказаної ширини і висоти.
     */
    public Mat resize(Mat image, int width, int height) {
        Mat resized = new Mat();
        opencv_imgproc.resize(image, resized, new Size(width, height));
        return resized;
    }

    /**
     * Конвертує кольорове зображення в градації сірого.
     */
    public Mat toGray(Mat image) {
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    /**
     * Нормалізує значення пікселів у межах 0–1 (float).
     */
    public Mat normalize(Mat image) {
        Mat normalized = new Mat();
        image.convertTo(normalized, opencv_core.CV_32F, 1.0 / 255.0, 0.0);
        return normalized;
    }

    /**
     * Виводить розміри зображення у консоль (для дебагу).
     */
    public void printImageInfo(Mat image, String label) {
        System.out.println(label + ": " + image.cols() + "x" + image.rows() + ", channels: " + image.channels());
    }

    /**
     * Експортує OpenCV Mat зображення у формат Base64 (PNG).
     *
     * @param mat Матриця зображення
     * @return base64 рядок зображення
     */
    public static String encode(Mat mat) {
        var buf = new org.bytedeco.javacpp.BytePointer();
        opencv_imgcodecs.imencode(".png", mat, buf);
        byte[] byteArray = new byte[(int) buf.limit()];
        buf.get(byteArray);
        buf.deallocate();
        return Base64.getEncoder().encodeToString(byteArray);
    }

    /**
     * Експортує Mat у масив байтів у заданому форматі (наприклад, ".png").
     */
    public byte[] matToBytes(Mat mat, String format) {
        String lowerFormat = format.toLowerCase();
        if (lowerFormat.equals(".dcm")) {
            throw new UnsupportedOperationException("DICOM export is not supported.");
        }

        BytePointer buffer = new BytePointer();
        boolean success = opencv_imgcodecs.imencode(lowerFormat, mat, buffer);

        if (!success) {
            throw new RuntimeException("Failed to encode image to format: " + format);
        }

        byte[] bytes = new byte[(int) buffer.limit()];
        buffer.get(bytes);
        buffer.deallocate(); // звільняємо ресурси

        return bytes;
    }

    /**
     * Зберігає Mat у файл.
     */
    public void saveMatToFile(Mat mat, File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".dcm")) {
            throw new UnsupportedOperationException("Saving to DICOM is not implemented.");
        }

        boolean success = opencv_imgcodecs.imwrite(file.getAbsolutePath(), mat);
        if (!success) {
            throw new RuntimeException("Failed to write image to file: " + file.getAbsolutePath());
        }
    }
}