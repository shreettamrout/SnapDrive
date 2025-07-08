package com.shreettam.mini_google_drive.service;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service("minioFileStorageService")
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioFileStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String upload(MultipartFile file, String fileKey) {
        try (InputStream inputStream = file.getInputStream()) {

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(args);

            return getPublicUrl(fileKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public byte[] download(String fileKey) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileKey)
                .build())) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(buf)) != -1) {
                outputStream.write(buf, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return String.format("http://localhost:9000/%s/%s", bucketName, fileKey);
    }

    @Override
    public String initiateMultipartUpload(String filename, String contentType) {
        throw new UnsupportedOperationException("Multipart upload not supported yet.");
    }
}
