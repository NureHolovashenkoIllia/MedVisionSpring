package ua.nure.holovashenko.medvisionspring.svm;

import java.io.File;
import java.util.List;
import java.util.Map;

public record Dataset(List<File> images, List<Integer> labels, Map<String, Integer> labelMap) {}
