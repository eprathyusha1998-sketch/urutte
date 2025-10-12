package com.urutte.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ProfilePictureService {

    @Value("${app.upload.profile-dir:./uploads/profiles}")
    private String profileUploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Download and store a profile picture from an external URL
     */
    public String downloadAndStoreProfilePicture(String externalUrl, String userId) throws IOException {
        if (!StringUtils.hasText(externalUrl)) {
            return null;
        }

        try {
            // Create profile upload directory if it doesn't exist
            Path uploadPath = Paths.get(profileUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Download the image
            URL url = new URL(externalUrl);
            try (InputStream in = url.openStream()) {
                // Get file extension from URL or default to jpg
                String fileExtension = getFileExtensionFromUrl(externalUrl);
                if (fileExtension.isEmpty()) {
                    fileExtension = ".jpg";
                }

                // Generate unique filename
                String filename = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
                Path targetPath = uploadPath.resolve(filename);

                // Copy the file
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Return the relative path for serving
                return "profiles/" + filename;
            }
        } catch (Exception e) {
            // Log the error but don't fail the user creation
            System.err.println("Failed to download profile picture from " + externalUrl + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get file extension from URL
     */
    private String getFileExtensionFromUrl(String url) {
        try {
            String path = new URL(url).getPath();
            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex != -1 && lastDotIndex < path.length() - 1) {
                String extension = path.substring(lastDotIndex + 1).toLowerCase();
                if (ALLOWED_EXTENSIONS.contains(extension)) {
                    return "." + extension;
                }
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return ".jpg";
    }

    /**
     * Generate a default avatar path for a user
     */
    public String generateDefaultAvatar(String userId) {
        // Use a simple hash to consistently assign one of the default avatars
        int avatarIndex = Math.abs(userId.hashCode() % 12) + 1; // 1-12
        return "/assets/images/avatars/avatar-" + avatarIndex + ".jpg";
    }
}
