package ua.nure.holovashenko.medvisionspring.svm;

public interface SvmClient {
    SvmClassificationResult classify(SvmClassificationRequest request);
}
