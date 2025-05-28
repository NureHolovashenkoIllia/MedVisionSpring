package ua.nure.holovashenko.medvisionspring.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.info.Info;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AppConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("https://medvision-app-eebbebcgb4fpf7e7.centralus-01.azurewebsites.net");
        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("MedVision API")
                        .version("1.0")
                        .description("MedVision API documentation on how to use the system."));
    }

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/pdf/**")
                        .addResourceLocations("classpath:/static/pdf/");
            }
        };
    }
}
