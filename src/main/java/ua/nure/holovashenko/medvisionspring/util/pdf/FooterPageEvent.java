package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.*;

public class FooterPageEvent extends PdfPageEventHelper {
    private final Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        Phrase footer1 = new Phrase("Звіт сформовано автоматизованою системою MedVision", footerFont);
        Phrase footer2 = new Phrase("*MedVision є дослідницьким проєктом і не є медичним інструментом для постановки діагнозу.", footerFont);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer1,
                (document.right() + document.left()) / 2, document.bottom() - 15, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer2,
                (document.right() + document.left()) / 2, document.bottom() - 30, 0);
    }
}
