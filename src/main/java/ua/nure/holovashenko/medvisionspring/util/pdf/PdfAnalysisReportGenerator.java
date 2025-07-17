package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ua.nure.holovashenko.medvisionspring.entity.ImageAnalysis;

import java.io.*;
import java.time.format.DateTimeFormatter;

import static ua.nure.holovashenko.medvisionspring.util.pdf.PdfComparisonReportUtil.loadWatermarkBytes;

public class PdfAnalysisReportGenerator {

    public static byte[] generateAnalysisPdf(ImageAnalysis analysis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        byte[] watermarkBytes = loadWatermarkBytes();

        writer.setPageEvent(new WatermarkPageEvent(watermarkBytes));
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
        Paragraph title = new Paragraph("Медичний звіт результатів КТ обстеження", PdfStyles.titleFont());
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private static void addAnalysisTable(Document document, ImageAnalysis analysis) throws DocumentException {
        PdfPTable table = PdfTableBuilder.createTwoColumnTable();

        table.addCell(PdfTableBuilder.headerCell("Номер обстеження"));
        table.addCell(PdfTableBuilder.textCell(String.valueOf(analysis.getImageAnalysisId())));

        table.addCell(PdfTableBuilder.headerCell("Деталі обстеження"));
        table.addCell(PdfTableBuilder.textCell(analysis.getAnalysisDetails()));

        table.addCell(PdfTableBuilder.headerCell("Діагноз"));
        table.addCell(PdfTableBuilder.textCell(analysis.getAnalysisDiagnosis()));

        table.addCell(PdfTableBuilder.headerCell("Рекомендації щодо лікування"));
        table.addCell(PdfTableBuilder.textCell(analysis.getTreatmentRecommendations()));

        table.addCell(PdfTableBuilder.headerCell("Загальна точність класифікації"));
        table.addCell(PdfTableBuilder.textCell(String.format("%.2f", analysis.getAnalysisAccuracy())));

        table.addCell(PdfTableBuilder.headerCell("Точність виявлення патологій"));
        table.addCell(PdfTableBuilder.textCell(String.format("%.2f", analysis.getAnalysisPrecision())));

        table.addCell(PdfTableBuilder.headerCell("Повнота виявлення патологій"));
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
        heatmap.scaleToFit(300, 300);
        heatmap.setAlignment(Image.ALIGN_CENTER);
        document.add(heatmap);
    }
}