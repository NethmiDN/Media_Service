package com.example.mediaservice.service;

import com.example.mediaservice.model.PhotoMetadata;
import com.example.mediaservice.repository.PhotoMetadataRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaStorageService {

    private final PhotoMetadataRepository photoMetadataRepository;

    // GCS Storage will be injected if spring-cloud-gcp is fully configured.
    // We mark it not required or use ObjectProvider to prevent startup errors without credentials.
    private final org.springframework.beans.factory.ObjectProvider<Storage> storageProvider;

    @Value("${app.gallery.use-gcs:false}")
    private boolean useGcs;

    @Value("${app.gallery.gcs-bucket:tripdiary-bucket}")
    private String gcsBucket;

    @Value("${app.gallery.upload-dir:uploads/}")
    private String uploadDir;

    public PhotoMetadata uploadPhoto(MultipartFile file, Long userId, String destinationId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFileName = UUID.randomUUID().toString() + extension;
        String photoUrl;

        if (useGcs) {
            // Upload to Google Cloud Storage
            Storage storage = storageProvider.getIfAvailable();
            if (storage == null) {
                throw new IllegalStateException("GCS Storage bean not available but use-gcs is true");
            }
            BlobInfo blobInfo = BlobInfo.newBuilder(gcsBucket, newFileName)
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());
            photoUrl = "https://storage.googleapis.com/" + gcsBucket + "/" + newFileName;
        } else {
            // Local fallback
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Build local URL pointing to the controller's static serve (or just a relative path)
            photoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/media/files/")
                    .path(newFileName)
                    .toUriString();
        }

        PhotoMetadata metadata = PhotoMetadata.builder()
                .userId(userId)
                .destinationId(destinationId)
                .fileName(newFileName)
                .contentType(file.getContentType())
                .photoUrl(photoUrl)
                .build();

        return photoMetadataRepository.save(metadata);
    }

    public List<PhotoMetadata> getPhotosByUser(Long userId) {
        return photoMetadataRepository.findByUserId(userId);
    }

    public List<PhotoMetadata> getPhotosByDestination(String destinationId) {
        return photoMetadataRepository.findByDestinationId(destinationId);
    }
}
