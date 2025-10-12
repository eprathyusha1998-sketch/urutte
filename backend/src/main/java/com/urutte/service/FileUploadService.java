package com.urutte.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {
    
    @Value("${app.upload.video-dir:./uploads/videos}")
    private String videoUploadDir;
    
    @Value("${app.upload.image-dir:./uploads/images}")
    private String imageUploadDir;
    
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList(
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv"
    );
    
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp"
    );
    
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3MB in bytes
    
    public String uploadVideo(MultipartFile file) throws IOException {
        // Validate file
        validateVideoFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(videoUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return "videos/" + filename;
    }
    
    public String uploadImage(MultipartFile file) throws IOException {
        // Validate file
        validateImageFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(imageUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return "images/" + filename;
    }
    
    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Video file is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Video file size exceeds 3MB limit");
        }
        
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_VIDEO_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid video format. Allowed formats: " + ALLOWED_VIDEO_EXTENSIONS);
        }
        
        // Basic content type validation
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("File is not a video");
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file size exceeds 3MB limit");
        }
        
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image format. Allowed formats: " + ALLOWED_IMAGE_EXTENSIONS);
        }
        
        // Basic content type validation
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File is not an image");
        }
    }
    
    public byte[] getVideo(String filename) throws IOException {
        Path filePath = Paths.get(videoUploadDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("Video file not found: " + filename);
        }
        return Files.readAllBytes(filePath);
    }
    
    public byte[] getImage(String filename) throws IOException {
        Path filePath = Paths.get(imageUploadDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("Image file not found: " + filename);
        }
        return Files.readAllBytes(filePath);
    }
    
    public void deleteVideo(String filename) throws IOException {
        Path filePath = Paths.get(videoUploadDir).resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
    
    public void deleteImage(String filename) throws IOException {
        Path filePath = Paths.get(imageUploadDir).resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}
