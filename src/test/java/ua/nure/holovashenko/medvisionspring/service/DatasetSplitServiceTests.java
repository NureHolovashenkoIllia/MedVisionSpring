package ua.nure.holovashenko.medvisionspring.service;

import org.junit.jupiter.api.Test;
import ua.nure.holovashenko.medvisionspring.svm.Dataset;
import ua.nure.holovashenko.medvisionspring.svm.DatasetSplitResult;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasetSplitServiceTests {

    private final DatasetSplitService service = new DatasetSplitService();

    @Test
    void createsReproducibleStratifiedSplit() {
        Dataset dataset = new Dataset(
                List.of(
                        new File("a1.png"),
                        new File("a2.png"),
                        new File("a3.png"),
                        new File("a4.png"),
                        new File("b1.png"),
                        new File("b2.png"),
                        new File("b3.png"),
                        new File("b4.png")
                ),
                List.of(0, 0, 0, 0, 1, 1, 1, 1),
                Map.of("a", 0, "b", 1)
        );

        DatasetSplitResult split = service.stratifiedSplit(dataset, 0.5, 0.25, 0.25, 42);

        assertThat(split.trainImages()).hasSize(4);
        assertThat(split.validationImages()).hasSize(2);
        assertThat(split.testImages()).hasSize(2);
        assertThat(service.countByClass(split.trainLabels())).containsEntry(0, 2).containsEntry(1, 2);
    }

    @Test
    void rejectsInvalidRatios() {
        Dataset dataset = new Dataset(List.of(), List.of(), Map.of());

        assertThatThrownBy(() -> service.stratifiedSplit(dataset, 0.8, 0.2, 0.2, 42))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Train, validation and test ratios must sum to 1.0");
    }
}
