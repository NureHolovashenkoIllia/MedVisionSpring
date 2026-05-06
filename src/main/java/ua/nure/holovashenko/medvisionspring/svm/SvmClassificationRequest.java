package ua.nure.holovashenko.medvisionspring.svm;

import java.io.File;

public record SvmClassificationRequest(
        File imageFile,
        boolean generateHeatmap
) {}
