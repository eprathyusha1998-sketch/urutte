package com.urutte.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.upload.profile-dir:./uploads/profiles}")
    private String profileUploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        Path uploadPath = Paths.get(uploadDir);
        String uploadPathStr = uploadPath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPathStr + "/");
        
        // Serve profile pictures
        Path profilePath = Paths.get(profileUploadDir);
        String profilePathStr = profilePath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/profiles/**")
                .addResourceLocations("file:" + profilePathStr + "/");
    }
}