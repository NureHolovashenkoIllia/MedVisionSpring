package ua.nure.holovashenko.medvisionspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoundingBox {
    private Long analysisNoteId;
    private int noteAreaX;
    private int noteAreaY;
    private int noteAreaWidth;
    private int noteAreaHeight;
}
