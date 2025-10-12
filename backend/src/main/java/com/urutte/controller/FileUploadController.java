package com.urutte.controller;

import com.urutte.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @PostMapping("/video")
    public ResponseEntity<Map<String, String>> uploadVideo(
            @RequestParam("video") MultipartFile file,
            @AuthenticationPrincipal OidcUser principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            String videoPath = fileUploadService.uploadVideo(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("videoUrl", "/api/videos/" + videoPath);
            response.put("message", "Video uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("error", "Failed to upload video");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            byte[] imageData = fileUploadService.getImage(filename);
            ByteArrayResource resource = new ByteArrayResource(imageData);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/jpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/videos/{filename:.+}")
    public ResponseEntity<Resource> getVideo(@PathVariable String filename) {
        try {
            byte[] videoData = fileUploadService.getVideo(filename);
            ByteArrayResource resource = new ByteArrayResource(videoData);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
