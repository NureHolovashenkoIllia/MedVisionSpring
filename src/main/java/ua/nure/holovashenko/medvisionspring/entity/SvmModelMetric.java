package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.nure.holovashenko.medvisionspring.enums.DatasetSplit;

@Entity
@Table(name = "svm_model_metric")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SvmModelMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id", nullable = false)
    private Long metricId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_version_id", nullable = false)
    private SvmModelVersion modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_split", nullable = false, length = 32)
    private DatasetSplit datasetSplit;

    @Column(name = "class_label", length = 64)
    private String classLabel;

    @Column(name = "metric_name", nullable = false, length = 64)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;
}
