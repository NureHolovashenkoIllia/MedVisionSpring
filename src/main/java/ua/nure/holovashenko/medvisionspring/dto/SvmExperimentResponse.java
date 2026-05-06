package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SvmExperimentResponse {
    private String status;
    private int trainCount;
    private int validationCount;
    private int testCount;
    private Map<String, Integer> labelMap;
    private Map<Integer, Integer> trainClassCounts;
    private Map<Integer, Integer> validationClassCounts;
    private Map<Integer, Integer> testClassCounts;
}
