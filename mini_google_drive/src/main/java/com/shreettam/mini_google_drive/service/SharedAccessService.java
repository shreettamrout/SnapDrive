package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.dto.ShareRequestDto;
import com.shreettam.mini_google_drive.dto.SharedAccessDto;
import com.shreettam.mini_google_drive.exception.AccessDeniedException;
import com.shreettam.mini_google_drive.exception.InvalidOperationException;
import com.shreettam.mini_google_drive.model.File;
import com.shreettam.mini_google_drive.model.SharedAccess;
import com.shreettam.mini_google_drive.model.User;
import com.shreettam.mini_google_drive.model.SharedAccess.SharedAccessId;
import com.shreettam.mini_google_drive.repository.SharedAccessRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SharedAccessService {

    private final SharedAccessRepository sharedAccessRepository;
    private final UserService userService;
    private final FileQueryService fileQueryService;
    private final NotificationService notificationService;

    public SharedAccessService(SharedAccessRepository sharedAccessRepository,
                                UserService userService,
                                FileQueryService fileQueryService,
                                NotificationService notificationService) {
        this.sharedAccessRepository = sharedAccessRepository;
        this.userService = userService;
        this.fileQueryService = fileQueryService;
        this.notificationService = notificationService;
    }

    public void shareFile(ShareRequestDto shareRequest, Long ownerId) {
        File file = fileQueryService.getFileById(shareRequest.fileId());
        validateOwnership(file, ownerId);

        User recipient = userService.getUserById(shareRequest.userId());
        preventSelfSharing(ownerId, recipient.getId());

        SharedAccess.PermissionType permission = shareRequest.permission();

        if (sharedAccessRepository.existsByFileAndUser(file, recipient)) {
            throw new IllegalStateException("This file is already shared with this user.");
        }

        SharedAccess share = new SharedAccess(file, recipient, permission);
        sharedAccessRepository.save(share);

        notificationService.sendShareNotification(
            recipient.getEmail(),
            file.getName(),
            permission.toString(),
            file.getOwner().getName()
        );
    }

    public void updateShare(Long fileId, Long userId, String newPermission, Long ownerId) {
        SharedAccessId id = new SharedAccessId(fileId, userId);
        SharedAccess share = sharedAccessRepository.findById(id)
        	    .orElseThrow(() -> new IllegalArgumentException(
        	        "Shared access not found for fileId: " + id.getFileId() + ", userId: " + id.getUserId()
        	    ));

        validateOwnership(share.getFile(), ownerId);

        SharedAccess.PermissionType permission = SharedAccess.PermissionType.valueOf(
            newPermission.toUpperCase()
        );

        share.setPermissionType(permission);
        sharedAccessRepository.save(share);
    }

    public void revokeShare(Long fileId, Long userId, Long ownerId) {
        SharedAccessId id = new SharedAccessId(fileId, userId);
        SharedAccess share = sharedAccessRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Shared access not found for fileId=" + fileId + ", userId=" + userId));

        validateOwnership(share.getFile(), ownerId);
        sharedAccessRepository.delete(share);
    }

    public void revokeAllAccess(Long fileId, Long ownerId) {
        File file = fileQueryService.getFileById(fileId);
        validateOwnership(file, ownerId);
        sharedAccessRepository.deleteByFileId(fileId);
    }

    public List<SharedAccessDto> getFileShares(Long fileId, Long ownerId) {
        File file = fileQueryService.getFileById(fileId);
        validateOwnership(file, ownerId);

        return sharedAccessRepository.findByFileId(fileId).stream()
            .map(share -> new SharedAccessDto(
                null,
                share.getUser().getId(),
                share.getUser().getName(),
                share.getUser().getEmail(),
                share.getPermissionType().name(),
                share.getSharedAt()
            ))
            .toList();
    }

    public void verifyReadAccess(Long fileId, Long userId) {
        if (!sharedAccessRepository.existsByFileIdAndUserIdAndPermissionType(
            fileId, userId, SharedAccess.PermissionType.READ)) {
            throw new AccessDeniedException("No read permission for file " + fileId);
        }
    }

    public void verifyWriteAccess(Long fileId, Long userId) {
        if (!sharedAccessRepository.existsByFileIdAndUserIdAndPermissionType(
            fileId, userId, SharedAccess.PermissionType.WRITE)) {
            throw new AccessDeniedException("No write permission for file " + fileId);
        }
    }

    private void validateOwnership(File file, Long claimedOwnerId) {
        if (!file.getOwner().getId().equals(claimedOwnerId)) {
            throw new AccessDeniedException("Only file owner can manage shares");
        }
    }

    private void preventSelfSharing(Long ownerId, Long recipientId) {
        if (ownerId.equals(recipientId)) {
            throw new InvalidOperationException("Cannot share file with yourself");
        }
    }
}
