package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String externalLocation = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!externalLocation.endsWith("/")) {
            externalLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(externalLocation, "classpath:/static/uploads/");
    }
}
