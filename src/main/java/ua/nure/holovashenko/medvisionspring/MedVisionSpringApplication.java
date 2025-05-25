package ua.nure.holovashenko.medvisionspring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
        "ua.nure.holovashenko.medvisionspring.util", // якщо є інші утиліти
        "ua.nure.holovashenko.medvisionspring.svm"
})
@EntityScan(basePackages = {
        "ua.nure.holovashenko.medvisionspring.entity"
})
@EnableJpaRepositories(basePackages = {
        "ua.nure.holovashenko.medvisionspring.repository"
})
public class MedVisionSpringApplication {

    public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.load();
//        System.setProperty("MYSQL_USER", dotenv.get("MYSQL_USER"));
//        System.setProperty("MYSQL_PASSWORD", dotenv.get("MYSQL_PASSWORD"));

        SpringApplication.run(MedVisionSpringApplication.class, args);
    }

}
