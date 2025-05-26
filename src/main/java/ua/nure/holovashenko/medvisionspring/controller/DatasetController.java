package ua.nure.holovashenko.medvisionspring.controller;

import ua.nure.holovashenko.medvisionspring.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dataset")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @PostMapping("/build")
    public String buildDataset() {
        return datasetService.buildDataset();
    }
}
