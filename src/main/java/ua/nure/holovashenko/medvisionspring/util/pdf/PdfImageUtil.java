package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfImageUtil {

    public static Image loadImageFromPath(String path) throws IOException, BadElementException {
        byte[] imageBytes;

        if (path.startsWith("http://") || path.startsWith("https://")) {
            // Завантаження з HTTP(S)
            try (InputStream in = URI.create(path).toURL().openStream()) {
                imageBytes = in.readAllBytes();
            }
        } else if (path.startsWith("file:/")) {
            // Завантаження з file: URI
            Path filePath = Paths.get(URI.create(path));
            imageBytes = Files.readAllBytes(filePath);
        } else {
            // Звичайний локальний шлях (без file:)
            Path filePath = Paths.get(path);
            imageBytes = Files.readAllBytes(filePath);
        }

        return Image.getInstance(imageBytes);
    }

    public static Image createImage(byte[] imageBytes, float width, float height) throws BadElementException, IOException {
        Image image = Image.getInstance(imageBytes);
        image.scaleToFit(width, height);
        image.setAlignment(Image.ALIGN_CENTER);
        return image;
    }
}
