package com.example.mediaservice.controller;

import com.example.mediaservice.model.PhotoMetadata;
import com.example.mediaservice.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStorageService galleryService;

    @PostMapping("/upload")
    public ResponseEntity<PhotoMetadata> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String destinationId) {
        try {
            PhotoMetadata metadata = galleryService.uploadPhoto(file, userId, destinationId);
            return ResponseEntity.ok(metadata);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PhotoMetadata>> getPhotosByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(galleryService.getPhotosByUser(userId));
    }

    @GetMapping("/event/{destinationId}")
    public ResponseEntity<List<PhotoMetadata>> getPhotosByDestination(@PathVariable String destinationId) {
        return ResponseEntity.ok(galleryService.getPhotosByDestination(destinationId));
    }

    // Endpoint to serve local files for testing
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads/").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}