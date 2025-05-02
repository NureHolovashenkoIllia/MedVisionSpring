package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.*;

public class PdfTableBuilder {

    public static PdfPTable createTwoColumnTable() throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        table.setWidths(new float[]{2, 5});
        return table;
    }

    public static PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, PdfStyles.labelFont()));
        cell.setBackgroundColor(new Color(230, 230, 250));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }

    public static PdfPCell textCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, PdfStyles.textFont()));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }
}
