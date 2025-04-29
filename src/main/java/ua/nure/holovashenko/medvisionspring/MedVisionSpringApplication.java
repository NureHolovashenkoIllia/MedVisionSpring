package ua.nure.holovashenko.medvisionspring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MedVisionSpringApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("MYSQL_USER", dotenv.get("MYSQL_USER"));
        System.setProperty("MYSQL_PASSWORD", dotenv.get("MYSQL_PASSWORD"));

        SpringApplication.run(MedVisionSpringApplication.class, args);
    }

}
