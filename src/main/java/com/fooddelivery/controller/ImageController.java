package com.fooddelivery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    private static final String UPLOAD_DIR = "uploads/images/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("filename", uniqueFilename);
            response.put("originalFilename", originalFilename);
            response.put("url", "/uploads/images/" + uniqueFilename);
            response.put("size", file.getSize());
            response.put("contentType", contentType);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }

    @PostMapping("/upload-restaurant")
    public ResponseEntity<?> uploadRestaurantImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("restaurantId") Long restaurantId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file");
            }

            String restaurantDir = UPLOAD_DIR + "restaurants/" + restaurantId + "/";
            File dir = new File(restaurantDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "restaurant_" + restaurantId + "_" + System.currentTimeMillis() +
                    file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            Path filePath = Paths.get(restaurantDir + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", filename);
            response.put("url", "/uploads/images/restaurants/" + restaurantId + "/" + filename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/upload-menu-item")
    public ResponseEntity<?> uploadMenuItemImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("menuItemId") Long menuItemId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file");
            }

            String menuDir = UPLOAD_DIR + "menu-items/";
            File dir = new File(menuDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "menu_" + menuItemId + "_" + System.currentTimeMillis() +
                    file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            Path filePath = Paths.get(menuDir + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", filename);
            response.put("url", "/uploads/images/menu-items/" + filename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            byte[] image = Files.readAllBytes(filePath);

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/restaurants/{restaurantId}/{filename:.+}")
    public ResponseEntity<?> getRestaurantImage(
            @PathVariable Long restaurantId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + "restaurants/" + restaurantId + "/" + filename);
            byte[] image = Files.readAllBytes(filePath);

            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/menu-items/{filename:.+}")
    public ResponseEntity<?> getMenuItemImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + "menu-items/" + filename);
            byte[] image = Files.readAllBytes(filePath);

            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<?> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.deleteIfExists(filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete image");
        }
    }
}