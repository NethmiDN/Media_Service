package com.example.mediaservice.repository;

import com.example.mediaservice.model.PhotoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoMetadataRepository extends JpaRepository<PhotoMetadata, Long> {
    List<PhotoMetadata> findByUserId(Long userId);
    List<PhotoMetadata> findByDestinationId(String destinationId);
}