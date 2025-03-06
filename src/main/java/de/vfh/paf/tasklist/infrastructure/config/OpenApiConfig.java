package de.vfh.paf.tasklist.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI documentation.
 * This enables the Swagger UI for API exploration and testing.
 */
@Configuration
public class OpenApiConfig {

  @Value("${tasklist.app-name}")
  private String appName;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title(appName + " API")
            .description("Task List Application API - Domain-Driven Design Implementation")
            .version("v1.0.0")
            .contact(new Contact()
                .name("PAF 2024")
                .url("https://bht-berlin.de")
                .email("paf@bht-berlin.de"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")));
  }
}
