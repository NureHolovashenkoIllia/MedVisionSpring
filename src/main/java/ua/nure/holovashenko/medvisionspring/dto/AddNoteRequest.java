package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;

@Data
public class AddNoteRequest {
    private String noteText;
    private Integer noteAreaX;
    private Integer noteAreaY;
    private Integer noteAreaWidth;
    private Integer noteAreaHeight;
}