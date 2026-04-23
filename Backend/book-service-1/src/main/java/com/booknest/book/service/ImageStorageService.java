package com.booknest.book.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// Service handling file storage operations for book images
@Service
@Slf4j
public class ImageStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    // Save uploaded file to disk and return its relative URL path
    public String saveImage(MultipartFile file) {
        log.info("Saving uploaded image");

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select an image to upload");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("Invalid file name");
        }

        String extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();

        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new RuntimeException("Only image files are allowed (jpg, jpeg, png, gif, webp)");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Image size cannot exceed 5MB");
        }

        String uniqueFilename = UUID.randomUUID().toString().substring(0, 8) + "_" + originalFilename;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadDir);
            }

            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String savedPath = "uploads/books/" + uniqueFilename;
            log.info("Image saved: URL path {}", savedPath);
            return savedPath;

        } catch (IOException e) {
            log.error("Failed to save image: {}", e.getMessage());
            throw new RuntimeException("Failed to save image. Please try again.");
        }
    }

    // Delete existing image file from disk
    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty())
            return;

        try {
            Path path = Paths.get(imagePath);
            if (Files.deleteIfExists(path)) {
                log.info("Deleted image: {}", imagePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete image: {}", imagePath);
        }
    }
}
