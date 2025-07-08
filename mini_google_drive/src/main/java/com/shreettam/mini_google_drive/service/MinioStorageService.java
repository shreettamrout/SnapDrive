package com.shreettam.mini_google_drive.service;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service("s3StorageService") // keeps existing bean name for compatibility
public class MinioStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.public-url}") // http://localhost:9000/mini-drive-bucket
    private String publicUrl;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String upload(MultipartFile file, String fileKey) {
        try (InputStream inputStream = file.getInputStream()) {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return getPublicUrl(fileKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public byte[] download(String objectKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public String initiateMultipartUpload(String filename, String contentType) {
        // MinIO Java SDK does not expose multipart upload logic like AWS SDK directly.
        // Either simulate multipart manually or return a placeholder here.
        return generateObjectKey(filename); // treat as pre-uploaded objectKey
    }

    private String generateObjectKey(String originalFilename) {
        return "user-uploads/" + UUID.randomUUID() + "/" + originalFilename;
    }

    

    @Override
    public String getPublicUrl(String fileKey) {
    	return publicUrl.endsWith("/") ? publicUrl + fileKey : publicUrl + "/" + fileKey;
    
    }
}
