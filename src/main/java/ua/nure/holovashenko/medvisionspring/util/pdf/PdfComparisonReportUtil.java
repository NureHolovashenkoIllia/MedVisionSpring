package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import ua.nure.holovashenko.medvisionspring.dto.ComparisonReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfComparisonReportUtil {

    public static byte[] generateComparisonPdf(ComparisonReport report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            byte[] watermarkBytes = loadWatermarkBytes();
            writer.setPageEvent(new WatermarkPageEvent(watermarkBytes));
            writer.setPageEvent(new FooterPageEvent());

            document.open();

            addTitle(document);
            addComparisonDates(document, report);
            addComparisonTable(document, report);
            addImageComparison(document, report);
            addLegend(document);

        } catch (Exception e) {
            throw new RuntimeException("Помилка генерації PDF-звіту", e);
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    static byte[] loadWatermarkBytes() throws IOException {
        ClassPathResource logo = new ClassPathResource("static/medvision.png");
        try (InputStream inputStream = logo.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    private static void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Порівняння результатів КТ обстежень", PdfStyles.titleFont());
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10f);
        document.add(title);
    }

    private static void addComparisonDates(Document document, ComparisonReport report) throws DocumentException {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        LocalDateTime dateFrom = LocalDateTime.parse(report.getCreatedAtFrom(), inputFormatter);
        LocalDateTime dateTo = LocalDateTime.parse(report.getCreatedAtTo(), inputFormatter);

        String formattedDateFrom = dateFrom.format(outputFormatter);
        String formattedDateTo = dateTo.format(outputFormatter);

        Paragraph createdDates = new Paragraph(
                "Дата обстеження №" + report.getFromId() + ": " + formattedDateFrom + "\n" +
                        "Дата обстеження №" + report.getToId() + ": " + formattedDateTo,
                PdfStyles.textFont());

        createdDates.setSpacingAfter(10f);
        document.add(createdDates);
    }

    private static void addComparisonTable(Document document, ComparisonReport report) throws DocumentException {
        PdfPTable comparisonTable = PdfTableBuilder.buildComparisonTable(report);
        comparisonTable.setSpacingAfter(15f);
        document.add(comparisonTable);
    }

    private static void addImageComparison(Document document, ComparisonReport report) throws DocumentException {
        PdfPTable imageTable = PdfTableBuilder.buildImageComparisonTable(report);
        imageTable.setSpacingAfter(10f);
        document.add(imageTable);
    }

    private static void addLegend(Document document) throws DocumentException {
        Paragraph legend = new Paragraph(
                "*Темніші області — зменшення активації, світліші — збільшення.\n" +
                        "*Колірні відтінки вказують на зміни між аналізами TO та FROM.",
                PdfStyles.textFont());
        legend.setSpacingBefore(5f);
        document.add(legend);
    }
}