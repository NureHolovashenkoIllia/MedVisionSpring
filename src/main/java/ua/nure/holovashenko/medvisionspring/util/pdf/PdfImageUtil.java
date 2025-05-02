package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.BadElementException;
import com.lowagie.text.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PdfImageUtil {

    public static Image loadImageFromPath(String filePath) throws IOException, BadElementException {
        byte[] imageBytes = Files.readAllBytes(new File(filePath).toPath());
        return Image.getInstance(imageBytes);
    }
}
