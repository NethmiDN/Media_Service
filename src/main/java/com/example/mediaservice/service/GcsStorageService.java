package com.example.mediaservice.service;

import com.example.mediaservice.exception.FileStorageException;
import com.example.mediaservice.model.PhotoMetadata;
import com.example.mediaservice.repository.PhotoMetadataRepository;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcsStorageService {

    private final Storage storage;
    private final PhotoMetadataRepository photoMetadataRepository;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    /**
     * Uploads a file to GCS bucket and returns the full public GCS URL.
     *
     * @param file the MultipartFile to upload
     * @return full public GCS URL
     */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty or null file.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            storage.create(blobInfo, file.getBytes());
            log.info("Successfully uploaded file {} to GCS bucket {}", fileName, bucketName);

            return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
        } catch (IOException | StorageException e) {
            log.error("Error uploading file {} to GCS: {}", originalFilename, e.getMessage(), e);
            throw new FileStorageException("Failed to upload file to Google Cloud Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a file to GCS and persists photo metadata.
     *
     * @param file the MultipartFile to upload
     * @param userId optional user ID
     * @param destinationId optional destination ID
     * @return saved PhotoMetadata entity
     */
    public PhotoMetadata uploadPhoto(MultipartFile file, Long userId, String destinationId) {
        String publicUrl = uploadFile(file);
        String fileName = extractFileName(publicUrl);

        PhotoMetadata metadata = PhotoMetadata.builder()
                .userId(userId)
                .destinationId(destinationId)
                .fileName(fileName)
                .contentType(file.getContentType())
                .photoUrl(publicUrl)
                .build();

        return photoMetadataRepository.save(metadata);
    }

    /**
     * Deletes a file from GCS using either a filename or full public GCS URL.
     *
     * @param fileNameOrUrl the filename or public URL of the object to delete
     * @return true if object was deleted, false if object was not found
     */
    public boolean deleteFile(String fileNameOrUrl) {
        if (fileNameOrUrl == null || fileNameOrUrl.trim().isEmpty()) {
            throw new FileStorageException("Filename or URL to delete must not be empty.");
        }

        String fileName = extractFileName(fileNameOrUrl);

        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Successfully deleted object {} from bucket {}", fileName, bucketName);
            } else {
                log.warn("Object {} was not found in bucket {}", fileName, bucketName);
            }

            return deleted;
        } catch (StorageException e) {
            log.error("Error deleting file {} from GCS: {}", fileName, e.getMessage(), e);
            throw new FileStorageException("Failed to delete file from Google Cloud Storage: " + e.getMessage(), e);
        }
    }

    public List<PhotoMetadata> getPhotosByUser(Long userId) {
        return photoMetadataRepository.findByUserId(userId);
    }

    public List<PhotoMetadata> getPhotosByDestination(String destinationId) {
        return photoMetadataRepository.findByDestinationId(destinationId);
    }

    /**
     * Helper to extract object filename from a given string (URL or filename).
     */
    private String extractFileName(String fileNameOrUrl) {
        if (fileNameOrUrl.contains("/")) {
            return fileNameOrUrl.substring(fileNameOrUrl.lastIndexOf("/") + 1);
        }
        return fileNameOrUrl;
    }
}
