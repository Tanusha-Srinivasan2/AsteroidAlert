package com.star.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply CORS to all /api endpoints
                        .allowedOrigins(
                                "http://localhost:5173", // Your React app's development server
                                "http://localhost:3000"  // Common alternative for React dev server
                                // Add any other origins where your frontend might be hosted in the future (e.g., production URL)
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true) // Allow sending cookies/auth headers
                        .maxAge(3600); // How long the preflight response can be cached (1 hour)
            }
        };
    }
}
    