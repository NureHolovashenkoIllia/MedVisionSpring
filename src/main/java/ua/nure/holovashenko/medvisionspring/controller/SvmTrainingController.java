package ua.nure.holovashenko.medvisionspring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.holovashenko.medvisionspring.svm.SvmService;

@RestController
@RequestMapping("/api/svm")
@RequiredArgsConstructor
public class SvmTrainingController {

    private final SvmService svmService;

    /**
     * Тренує повну модель і патч-модель на вхідному датасеті.
     * @param datasetPath шлях до базової папки з класами
     */
    @PostMapping("/train")
    public ResponseEntity<String> trainBothModels(@RequestParam String datasetPath) {
        try {
            // Тренування повної моделі
            svmService.trainFromDirectory(datasetPath, false);
            svmService.saveModel("models/svm_full_model.xml", false);

            // Тренування патч-моделі
            svmService.trainFromDirectory(datasetPath, true);
            svmService.saveModel("models/svm_patch_model.xml", true);

            return ResponseEntity.ok("Моделі успішно натреновано та збережено.");
        } catch (Exception e) {
            e.printStackTrace(); // ← додай це
            return ResponseEntity.internalServerError()
                    .body("Помилка під час тренування: " + e.getMessage());
        }
    }
}
