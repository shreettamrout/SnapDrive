package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class LocalStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${app.storage.local.path}") String storagePath) {
        this.rootLocation = Paths.get(storagePath);
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize local storage", e);
        }
    }

    @Override
    public String upload(MultipartFile file, String fileKey) {
        try {
            Path destination = rootLocation.resolve(fileKey);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return fileKey;
        } catch (IOException e) {
            throw new StorageException("Failed to store file locally", e);
        }
    }

    @Override
    public byte[] download(String fileKey) {
        try {
            Path file = rootLocation.resolve(fileKey);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new StorageException("Failed to read file locally", e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            Path file = rootLocation.resolve(fileKey);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file locally", e);
        }
    }

    @Override
    public String getPublicUrl(String fileKey) {
        // For local dev, just return the filename or build a custom path
        return "/files/" + fileKey; // Assumes you expose this via a controller
    }

    @Override
    public String initiateMultipartUpload(String filename, String contentType) {
        throw new UnsupportedOperationException("Multipart upload is not supported in local storage.");
    }

}
