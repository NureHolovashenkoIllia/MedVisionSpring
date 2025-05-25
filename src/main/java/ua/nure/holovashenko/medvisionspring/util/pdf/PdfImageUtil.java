package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.BadElementException;
import com.lowagie.text.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

public class PdfImageUtil {

    public static Image loadImageFromPath(String path) throws IOException, BadElementException {
        byte[] imageBytes;

        if (path.startsWith("http://") || path.startsWith("https://")) {
            // Завантаження з URL через URI
            try (InputStream in = URI.create(path).toURL().openStream()) {
                imageBytes = in.readAllBytes();
            }
        } else {
            // Завантаження з локального шляху
            imageBytes = Files.readAllBytes(new File(path).toPath());
        }

        return Image.getInstance(imageBytes);
    }

}
