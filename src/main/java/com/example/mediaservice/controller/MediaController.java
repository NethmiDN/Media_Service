package com.example.mediaservice.controller;

import com.example.mediaservice.model.PhotoMetadata;
import com.example.mediaservice.service.GcsStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final GcsStorageService gcsStorageService;

    /**
     * Upload a file/image to Google Cloud Storage.
     *
     * @param file the MultipartFile to upload
     * @param userId optional user ID
     * @param destinationId optional destination ID
     * @return PhotoMetadata if userId/destinationId are supplied, or Map with public file URL
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String destinationId) {

        if (userId != null || destinationId != null) {
            PhotoMetadata metadata = gcsStorageService.uploadPhoto(file, userId, destinationId);
            return ResponseEntity.ok(metadata);
        }

        String fileUrl = gcsStorageService.uploadFile(file);
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully to GCS",
                "url", fileUrl
        ));
    }

    /**
     * Delete a file from Google Cloud Storage using filename or public GCS URL.
     *
     * @param file filename or public URL (param name: 'file')
     * @param fileNameOrUrl filename or public URL (param name: 'fileNameOrUrl')
     * @return deletion status response
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestParam(value = "file", required = false) String file,
            @RequestParam(value = "fileNameOrUrl", required = false) String fileNameOrUrl) {

        String target = (fileNameOrUrl != null && !fileNameOrUrl.isBlank()) ? fileNameOrUrl : file;
        boolean deleted = gcsStorageService.deleteFile(target);

        return ResponseEntity.ok(Map.of(
                "message", deleted ? "File deleted successfully from GCS" : "File not found in GCS",
                "deleted", deleted
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PhotoMetadata>> getPhotosByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(gcsStorageService.getPhotosByUser(userId));
    }

    @GetMapping("/event/{destinationId}")
    public ResponseEntity<List<PhotoMetadata>> getPhotosByDestination(@PathVariable String destinationId) {
        return ResponseEntity.ok(gcsStorageService.getPhotosByDestination(destinationId));
    }
}