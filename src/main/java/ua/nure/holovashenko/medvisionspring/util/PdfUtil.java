package ua.nure.holovashenko.medvisionspring.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;

import java.awt.Color;
import java.io.*;
import java.nio.file.Files;

public class PdfUtil {

    public static byte[] generateAnalysisPdf(ImageAnalysis analysis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 50); // лівий, правий, верхній, нижній відступи
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new FooterPageEvent());
        document.open();

        // === Шапка документа ===
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
        Paragraph title = new Paragraph("Медичний звіт про аналіз зображення", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // === Інформація про аналіз ===
        Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.HELVETICA, 12);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);

        infoTable.addCell(getCell("Номер аналізу:", labelFont));
        infoTable.addCell(getCell(String.valueOf(analysis.getImageAnalysisId()), textFont));

        infoTable.addCell(getCell("Діагноз:", labelFont));
        infoTable.addCell(getCell(analysis.getAnalysisDiagnosis(), textFont));

        infoTable.addCell(getCell("Точність:", labelFont));
        infoTable.addCell(getCell(String.format("%.2f", analysis.getAnalysisAccuracy()), textFont));

        infoTable.addCell(getCell("Точність (Precision):", labelFont));
        infoTable.addCell(getCell(String.format("%.2f", analysis.getAnalysisPrecision()), textFont));

        infoTable.addCell(getCell("Повнота (Recall):", labelFont));
        infoTable.addCell(getCell(String.format("%.2f", analysis.getAnalysisRecall()), textFont));

        infoTable.addCell(getCell("Пацієнт:", labelFont));
        infoTable.addCell(getCell(analysis.getPatient().getUserName(), textFont));

        infoTable.addCell(getCell("Лікар:", labelFont));
        infoTable.addCell(getCell(analysis.getDoctor().getUserName(), textFont));

        infoTable.addCell(getCell("Дата:", labelFont));
        infoTable.addCell(getCell(analysis.getCreationDatetime().toString(), textFont));

        document.add(infoTable);

        // === Зображення ===
        Paragraph imgTitle = new Paragraph("Теплова карта аналізованого зображення", labelFont);
        imgTitle.setSpacingBefore(10);
        imgTitle.setSpacingAfter(10);
        imgTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(imgTitle);

        byte[] heatmapBytes = Files.readAllBytes(new File(analysis.getHeatmapFile().getImageFileUrl()).toPath());
        Image heatmap = Image.getInstance(heatmapBytes);
        heatmap.scaleToFit(350, 350);
        heatmap.setAlignment(Image.ALIGN_CENTER);
        document.add(heatmap);

        document.close();
        return baos.toByteArray();
    }

    private static PdfPCell getCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }
}
