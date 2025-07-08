package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.*;
import com.shreettam.mini_google_drive.model.*;
import com.shreettam.mini_google_drive.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileStorageService storageService;
    private final UserService userService;
    private final SharedAccessService sharedAccessService;

    public FileService(FileRepository fileRepository,
                       FolderRepository folderRepository,
                       @Qualifier("s3StorageService") FileStorageService storageService,
                       UserService userService,
                       SharedAccessService sharedAccessService) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.storageService = storageService;
        this.userService = userService;
        this.sharedAccessService = sharedAccessService;
    }

    // File Upload
    public FileResponseDto uploadFile(MultipartFile file, Long folderId, Long ownerId, Boolean isPublic) throws FolderNotFoundException {
        User owner = userService.getUserById(ownerId);
        userService.verifyStorageAvailability(ownerId, file.getSize());

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId)
                    .filter(f -> f.getOwner().getId().equals(ownerId))
                    .orElseThrow(FolderNotFoundException::new);
        }

        Boolean visibility = isPublic != null && isPublic;

        String fileKey = "user_" + ownerId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        logger.info("Uploading file for user {} with key {}", ownerId, fileKey);

        storageService.upload(file, fileKey);

        File newFile = new File(
                file.getOriginalFilename(),
                fileKey,
                file.getContentType(),
                file.getSize(),
                visibility,
                owner,
                folder
        );

        File savedFile = fileRepository.save(newFile);
        logger.info("File uploaded and saved with ID {}", savedFile.getId());

        return mapToDto(savedFile);
    }

    // File Download
    public FileDownloadDto downloadFile(Long fileId, Long requesterId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if (!file.getOwner().getId().equals(requesterId)) {
            sharedAccessService.verifyReadAccess(fileId, requesterId);
        }

        byte[] fileData = storageService.download(file.getS3Key());
        logger.info("File {} downloaded by user {}", fileId, requesterId);

        return new FileDownloadDto(file.getName(), file.getMimeType(), fileData);
    }

    // File Deletion
    public void deleteFile(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        if (!file.getOwner().getId().equals(userId)) {
            sharedAccessService.verifyWriteAccess(fileId, userId);
        }

        storageService.delete(file.getS3Key());
        fileRepository.delete(file);
        long sizeToSubtract = file.getSize() != null ? file.getSize() : 0L;
        userService.updateStorageUsed(userId, -sizeToSubtract);

        logger.info("File {} deleted by user {}", fileId, userId);
    }

    // File Sharing
    public void shareFile(Long fileId, Long recipientId, String permission, Long ownerId) {
        fileRepository.findByIdAndOwnerId(fileId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Only owner can share files"));

        SharedAccess.PermissionType permissionType = SharedAccess.PermissionType.valueOf(permission.toUpperCase());
        ShareRequestDto request = new ShareRequestDto(fileId, recipientId, permissionType);
        sharedAccessService.shareFile(request, ownerId);

        logger.info("File {} shared with user {} with permission {}", fileId, recipientId, permission);
    }

    // File Search
    public List<FileResponseDto> searchFiles(Long userId, String query) {
        User user = userService.getUserById(userId);
        return fileRepository.searchByOwnerAndName(user, query).stream()
                .map(this::mapToDto)
                .toList();
    }

    public FileResponseDto mapToDto(File file) {
        // Secure internal access via API route
        String publicUrl = "/api/files/download/" + file.getId();

        return new FileResponseDto(
                file.getId(),
                file.getName(),
                publicUrl,
                file.getSize(),
                file.getMimeType(),
                file.getCreatedAt(),
                file.getFolder() != null ? file.getFolder().getId() : null
        );
    }

    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }
}
