package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import ua.nure.holovashenko.medvisionspring.dto.ComparisonReport;

import com.lowagie.text.*;

import java.awt.Color;
import java.util.Base64;

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

    public static PdfPTable buildComparisonTable(ComparisonReport report) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        table.addCell(new Phrase(""));
        table.addCell(new Phrase("Обстеження №" + report.getFromId().toString(), PdfStyles.labelFont()));
        table.addCell(new Phrase("Обстеження №" + report.getToId().toString(), PdfStyles.labelFont()));

        table.addCell(new Phrase("Деталі обстеження", PdfStyles.textFont()));
        table.addCell(new Phrase(report.getAnalysisDetailsFrom()));
        table.addCell(new Phrase(report.getAnalysisDetailsTo()));

        table.addCell(new Phrase("Діагноз", PdfStyles.textFont()));
        table.addCell(new Phrase(report.getDiagnosisTextFrom()));
        table.addCell(new Phrase(report.getDiagnosisTextTo()));

        table.addCell(new Phrase("Рекомендації щодо лікування", PdfStyles.textFont()));
        table.addCell(new Phrase(report.getTreatmentRecommendationsFrom()));
        table.addCell(new Phrase(report.getTreatmentRecommendationsTo()));

        table.addCell(new Phrase("Загальна точність класифікації", PdfStyles.textFont()));
        table.addCell(new Phrase(String.valueOf(report.getAccuracyFrom())));
        table.addCell(new Phrase(String.valueOf(report.getAccuracyTo())));

        table.addCell(new Phrase("Повнота виявлення патологій", PdfStyles.textFont()));
        table.addCell(new Phrase(String.valueOf(report.getRecallFrom())));
        table.addCell(new Phrase(String.valueOf(report.getRecallTo())));

        table.addCell(new Phrase("Точність виявлення патологій", PdfStyles.textFont()));
        table.addCell(new Phrase(String.valueOf(report.getPrecisionFrom())));
        table.addCell(new Phrase(String.valueOf(report.getPrecisionTo())));

        return table;
    }

    public static PdfPTable buildImageComparisonTable(ComparisonReport report) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        try {
            table.addCell(new Phrase("Обстеження №" + report.getFromId().toString(), PdfStyles.labelFont()));
            table.addCell(new Phrase("Обстеження №" + report.getToId().toString(), PdfStyles.labelFont()));
            table.addCell(new Phrase("Heatmap різниці", PdfStyles.labelFont()));

            byte[] fromBytes = Base64.getDecoder().decode(report.getFromImageBase64());
            Image fromImage = PdfImageUtil.createImage(fromBytes, 150, 150);
            fromImage.setAlt("Обстеження №" + report.getFromId());
            table.addCell(fromImage);

            byte[] toBytes = Base64.getDecoder().decode(report.getToImageBase64());
            Image toImage = PdfImageUtil.createImage(toBytes, 150, 150);
            toImage.setAlt("Обстеження №" + report.getToId());
            table.addCell(toImage);

            byte[] diffBytes = Base64.getDecoder().decode(report.getDiffHeatmap());
            Image diffImage = PdfImageUtil.createImage(diffBytes, 150, 150);
            diffImage.setAlt("Heatmap різниці");
            table.addCell(diffImage);
        } catch (Exception e) {
            PdfPCell errorCell = new PdfPCell(new Phrase("Помилка при завантаженні зображень", PdfStyles.textFont()));
            errorCell.setColspan(3);
            errorCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(errorCell);
        }

        return table;
    }
}
