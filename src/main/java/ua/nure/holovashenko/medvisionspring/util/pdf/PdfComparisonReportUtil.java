package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ua.nure.holovashenko.medvisionspring.dto.ComparisonReport;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class PdfComparisonReportUtil {

    public static byte[] generateComparisonPdf(ComparisonReport report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            // Заголовок
            Paragraph title = new Paragraph("Порівняння результатів КТ обстежень", PdfStyles.titleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Дата створення
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            LocalDateTime dateFrom = LocalDateTime.parse(report.getCreatedAtFrom(), inputFormatter);
            LocalDateTime dateTo = LocalDateTime.parse(report.getCreatedAtTo(), inputFormatter);

            String formattedDateFrom = dateFrom.format(outputFormatter);
            String formattedDateTo = dateTo.format(outputFormatter);

            Paragraph createdDates = new Paragraph(
                    "Дата обстеження №" + report.getFromId() + ": " + formattedDateFrom + "\nДата обстеження №" + report.getToId() + ": " + formattedDateTo,
                    PdfStyles.textFont());
            document.add(createdDates);
            document.add(Chunk.NEWLINE);

            // Таблиця діагностик
            PdfPTable comparisonTable = PdfTableBuilder.buildComparisonTable(report);
            document.add(comparisonTable);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Таблиця зображень (реалізована через PdfTableBuilder)
            PdfPTable imageTable = PdfTableBuilder.buildImageComparisonTable(report);
            document.add(imageTable);

            // Легенда
            Paragraph legend = new Paragraph(
                    "*Темніші області — зменшення активації, світліші — збільшення.\n" +
                            "*Колірні відтінки вказують на зміни між аналізами TO та FROM.",
                    PdfStyles.textFont());
            legend.setSpacingBefore(5f);
            document.add(legend);

        } catch (Exception e) {
            throw new RuntimeException("Помилка генерації PDF-звіту", e);
        } finally {
            document.close();
        }

        return out.toByteArray();
    }
}
