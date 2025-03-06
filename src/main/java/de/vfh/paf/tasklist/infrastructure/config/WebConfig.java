package de.vfh.paf.tasklist.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Web MVC related settings.
 * This includes CORS configuration for REST endpoints.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS for REST endpoints.
     * Allows requests from any origin, with common HTTP methods.
     *
     * @param registry The CorsRegistry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}