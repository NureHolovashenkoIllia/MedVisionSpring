package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.Font;

import java.awt.*;

public class PdfStyles {

    public static Font titleFont() {
        return new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
    }

    public static Font labelFont() {
        return new Font(Font.HELVETICA, 12, Font.BOLD);
    }

    public static Font textFont() {
        return new Font(Font.HELVETICA, 12);
    }
}
