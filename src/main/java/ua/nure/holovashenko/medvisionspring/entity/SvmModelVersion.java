package ua.nure.holovashenko.medvisionspring.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.nure.holovashenko.medvisionspring.enums.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "svm_model_version")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SvmModelVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_version_id", nullable = false)
    private Long modelVersionId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "version", nullable = false, unique = true, length = 64)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 32)
    private SvmModelType modelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "kernel_type", nullable = false, length = 32)
    private SvmKernelType kernelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "multiclass_strategy", length = 32)
    private SvmMulticlassStrategy multiclassStrategy;

    @Column(name = "feature_set", nullable = false, length = 255)
    private String featureSet;

    @Column(name = "image_width", nullable = false)
    private Integer imageWidth;

    @Column(name = "image_height", nullable = false)
    private Integer imageHeight;

    @Column(name = "patch_size")
    private Integer patchSize;

    @Column(name = "step_size")
    private Integer stepSize;

    @Column(name = "dataset_name", length = 255)
    private String datasetName;

    @Column(name = "dataset_hash", length = 128)
    private String datasetHash;

    @Column(name = "train_split_ratio")
    private Double trainSplitRatio;

    @Column(name = "validation_split_ratio")
    private Double validationSplitRatio;

    @Column(name = "test_split_ratio")
    private Double testSplitRatio;

    @Column(name = "storage_uri", nullable = false, length = 1000)
    private String storageUri;

    @Column(name = "metrics_uri", length = 1000)
    private String metricsUri;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SvmModelStatus status;

    @Column(name = "trained_at")
    private LocalDateTime trainedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
