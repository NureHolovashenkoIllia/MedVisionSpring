package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Size(max = 100)
    @Column(name = "position", length = 100)
    private String position;

    @Size(max = 100)
    @Column(name = "department", length = 100)
    private String department;

    @Size(max = 50)
    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @OneToOne
    @MapsId
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "doctor_id")
    private User user;
}