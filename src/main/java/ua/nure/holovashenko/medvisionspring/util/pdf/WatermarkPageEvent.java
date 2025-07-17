package ua.nure.holovashenko.medvisionspring.util.pdf;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;

public class WatermarkPageEvent extends PdfPageEventHelper {

    private final Image baseImage;
    private final float imageWidth;
    private final float imageHeight;

    public WatermarkPageEvent(byte[] imageBytes) throws BadElementException {
        try {
            this.baseImage = Image.getInstance(imageBytes);
            this.baseImage.scaleToFit(120, 120);
            this.imageWidth = this.baseImage.getScaledWidth();
            this.imageHeight = this.baseImage.getScaledHeight();
        } catch (Exception e) {
            throw new RuntimeException("Помилка під час завантаження зображення водяного знаку", e);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.saveState();

            PdfGState gState = new PdfGState();
            gState.setFillOpacity(0.07f);
            canvas.setGState(gState);

            float docWidth = document.getPageSize().getWidth();
            float docHeight = document.getPageSize().getHeight();

            boolean isOffsetRow = false;

            for (float y = document.bottom(); y < docHeight; y += imageHeight + 40) {
                float startX = document.left() + (isOffsetRow ? imageWidth / 2 : 0);

                for (float x = startX; x < docWidth; x += imageWidth + 40) {
                    Image image = Image.getInstance(baseImage);
                    image.setAbsolutePosition(x, y);
                    canvas.addImage(image);
                }

                isOffsetRow = !isOffsetRow;
            }

            canvas.restoreState();

        } catch (DocumentException e) {
            throw new RuntimeException("Не вдалося додати водяний знак", e);
        }
    }
}