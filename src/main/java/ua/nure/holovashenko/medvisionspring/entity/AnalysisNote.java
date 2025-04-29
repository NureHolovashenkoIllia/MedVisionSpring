package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_note_id", nullable = false)
    private Long analysisNoteId;

    @Lob
    @Column(name = "note_text", nullable = false, columnDefinition = "text")
    private String noteText;

    @Column(name = "note_area_x")
    private Integer noteAreaX;

    @Column(name = "note_area_y")
    private Integer noteAreaY;

    @Column(name = "note_area_width")
    private Integer noteAreaWidth;

    @Column(name = "note_area_height")
    private Integer noteAreaHeight;

    @Column(name = "creation_datetime")
    private LocalDateTime creationDatetime;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "image_analysis_id")
    private ImageAnalysis imageAnalysis;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
