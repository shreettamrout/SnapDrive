package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.exception.StorageQuotaExceededException;
import com.shreettam.mini_google_drive.model.User;
import com.shreettam.mini_google_drive.repository.UserRepository;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StorageQuotaService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public StorageQuotaService(UserRepository userRepository, MinioClient minioClient) {
        this.userRepository = userRepository;
        this.minioClient = minioClient;
    }

    public boolean hasEnoughSpace(Long userId, long fileSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return (user.getStorageQuota() - user.getUsedStorage()) >= fileSize;
    }

    @Transactional
    public void increaseStorageUsed(Long userId, long fileSize) {
        if (!hasEnoughSpace(userId, fileSize)) {
            throw new StorageQuotaExceededException("Storage quota exceeded. Required: " + fileSize + " bytes");
        }
        entityManager.createQuery(
                "UPDATE User u SET u.usedStorage = u.usedStorage + :fileSize WHERE u.id = :userId")
            .setParameter("fileSize", fileSize)
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Transactional
    public void decreaseStorageUsed(Long userId, long fileSize) {
        entityManager.createQuery(
                "UPDATE User u SET u.usedStorage = u.usedStorage - :fileSize WHERE u.id = :userId")
            .setParameter("fileSize", fileSize)
            .setParameter("userId", userId)
            .executeUpdate();
    }

    public long getStorageUsed(Long userId) {
        return userRepository.calculateStorageUsed(userId);
    }

    public long getStorageQuota(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getStorageQuota();
    }

    //  MinIO version of fetching file size
    public long getFileSizeFromMinIO(String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()
            );
            return stat.size();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch file size from MinIO: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void resetStorageUsed(Long userId) {
        entityManager.createQuery(
                "UPDATE User u SET u.usedStorage = 0 WHERE u.id = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }
}
