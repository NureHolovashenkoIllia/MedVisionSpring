package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.SvmKernelType;
import ua.nure.holovashenko.medvisionspring.enums.SvmModelType;
import ua.nure.holovashenko.medvisionspring.enums.SvmMulticlassStrategy;

@Data
public class SvmExperimentRequest {
    @NotBlank
    private String datasetPath;

    @NotNull
    private SvmModelType modelType;

    @NotNull
    private SvmKernelType kernelType;

    @NotNull
    private SvmMulticlassStrategy multiclassStrategy;

    @NotBlank
    private String featureSet;

    private Integer patchSize;
    private Integer stepSize;

    @DecimalMin("0.01")
    @DecimalMax("0.98")
    private double trainRatio = 0.7;

    @DecimalMin("0.01")
    @DecimalMax("0.98")
    private double validationRatio = 0.15;

    @DecimalMin("0.01")
    @DecimalMax("0.98")
    private double testRatio = 0.15;

    private long seed = 42L;
}
