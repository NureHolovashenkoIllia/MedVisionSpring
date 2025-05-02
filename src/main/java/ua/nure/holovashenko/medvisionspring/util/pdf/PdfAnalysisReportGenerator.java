package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;

import java.io.*;
import java.time.format.DateTimeFormatter;

public class PdfAnalysisReportGenerator {

    public static byte[] generateAnalysisPdf(ImageAnalysis analysis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new FooterPageEvent());
        document.open();

        addTitle(document);
        addAnalysisTable(document, analysis);
        addMetadata(document, analysis);
        addHeatmap(document, analysis);

        document.close();
        return baos.toByteArray();
    }

    private static void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Медичний звіт про аналіз зображення", PdfStyles.titleFont());
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private static void addAnalysisTable(Document document, ImageAnalysis analysis) throws DocumentException {
        PdfPTable table = PdfTableBuilder.createTwoColumnTable();

        table.addCell(PdfTableBuilder.headerCell("Номер аналізу"));
        table.addCell(PdfTableBuilder.textCell(String.valueOf(analysis.getImageAnalysisId())));

        table.addCell(PdfTableBuilder.headerCell("Діагноз"));
        table.addCell(PdfTableBuilder.textCell(analysis.getAnalysisDiagnosis()));

        table.addCell(PdfTableBuilder.headerCell("Точність"));
        table.addCell(PdfTableBuilder.textCell(String.format("%.2f", analysis.getAnalysisAccuracy())));

        table.addCell(PdfTableBuilder.headerCell("Precision"));
        table.addCell(PdfTableBuilder.textCell(String.format("%.2f", analysis.getAnalysisPrecision())));

        table.addCell(PdfTableBuilder.headerCell("Recall"));
        table.addCell(PdfTableBuilder.textCell(String.format("%.2f", analysis.getAnalysisRecall())));

        document.add(table);
    }

    private static void addMetadata(Document document, ImageAnalysis analysis) throws DocumentException {
        Font textFont = PdfStyles.textFont();

        document.add(new Paragraph("Пацієнт: " + analysis.getPatient().getUserName(), textFont));
        document.add(new Paragraph("Лікар: " + analysis.getDoctor().getUserName(), textFont));

        String formattedDate = analysis.getCreationDatetime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Paragraph dateInfo = new Paragraph("Дата: " + formattedDate, textFont);
        dateInfo.setSpacingAfter(15);
        document.add(dateInfo);
    }

    private static void addHeatmap(Document document, ImageAnalysis analysis) throws IOException, DocumentException {
        Paragraph imgTitle = new Paragraph("Теплова карта аналізованого зображення", PdfStyles.labelFont());
        imgTitle.setSpacingBefore(10);
        imgTitle.setSpacingAfter(10);
        imgTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(imgTitle);

        Image heatmap = PdfImageUtil.loadImageFromPath(analysis.getHeatmapFile().getImageFileUrl());
        heatmap.scaleToFit(350, 350);
        heatmap.setAlignment(Image.ALIGN_CENTER);
        document.add(heatmap);
    }
}
