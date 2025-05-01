package ua.nure.holovashenko.medvisionspring.svm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelMetrics {
    private float accuracy;
    private float precision;
    private float recall;
}
