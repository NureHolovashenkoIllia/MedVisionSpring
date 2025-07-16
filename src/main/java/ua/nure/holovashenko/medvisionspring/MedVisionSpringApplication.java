package ua.nure.holovashenko.medvisionspring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {
        "ua.nure.holovashenko.medvisionspring.config",
        "ua.nure.holovashenko.medvisionspring.controller",
        "ua.nure.holovashenko.medvisionspring.dto",
        "ua.nure.holovashenko.medvisionspring.entity",
        "ua.nure.holovashenko.medvisionspring.enums",
        "ua.nure.holovashenko.medvisionspring.exception",
        "ua.nure.holovashenko.medvisionspring.repository",
        "ua.nure.holovashenko.medvisionspring.security",
        "ua.nure.holovashenko.medvisionspring.service",
        "ua.nure.holovashenko.medvisionspring.storage",
        "ua.nure.holovashenko.medvisionspring.util",
        "ua.nure.holovashenko.medvisionspring.svm"
})
@EntityScan(basePackages = {
        "ua.nure.holovashenko.medvisionspring.entity"
})
@EnableJpaRepositories(basePackages = {
        "ua.nure.holovashenko.medvisionspring.repository"
})
@EnableAsync
public class MedVisionSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedVisionSpringApplication.class, args);
    }

}
