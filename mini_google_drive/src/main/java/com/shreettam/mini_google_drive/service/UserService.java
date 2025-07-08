package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.*;
import com.shreettam.mini_google_drive.model.*;
import com.shreettam.mini_google_drive.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository,
                     FileRepository fileRepository,
                     FolderRepository folderRepository,
                     PasswordEncoder passwordEncoder,
                     StorageQuotaService quotaService,
                     NotificationService notificationService) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    // Registration
    public User register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException(dto.email());
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setStorageQuota(10_737_418_240L); // 10GB default

        // Create root folder
        Folder rootFolder = new Folder("Root", "/", user, null);
        user.getFolders().add(rootFolder);

        User savedUser = userRepository.save(user);
        notificationService.sendWelcomeEmail(user.getEmail());
        return savedUser;
    }

    // Profile Management
    public UserProfileDto getProfile(Long userId) {
        User user = getUserById(userId);
        Long usedStorageValue = fileRepository.calculateTotalStorageUsed(userId);
        long usedStorage = (usedStorageValue != null) ? usedStorageValue : 0L;

        return new UserProfileDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getStorageQuota(),
            usedStorage
        );
    }

    public User updateProfile(Long userId, UserUpdateDto dto) {
        User user = getUserById(userId);
        
        if (dto.name() != null) {
            user.setName(dto.name());
        }
        
        if (dto.password() != null) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        
        return userRepository.save(user);
    }

    // Storage Operations
    public StorageInfoDto getStorageInfo(Long userId) {
        User user = getUserById(userId);
        Long usedStorageValue = fileRepository.calculateTotalStorageUsed(userId);
        long usedStorage = (usedStorageValue != null) ? usedStorageValue : 0L;
        return new StorageInfoDto(
            usedStorage,
            user.getStorageQuota(),
            (double) usedStorage / user.getStorageQuota() * 100
        );
    }

//    public void increaseStorageQuota(Long userId, long additionalBytes) {
//        User user = getUserById(userId);
//        user.setStorageQuota(user.getStorageQuota() + additionalBytes);
//        userRepository.save(user);
//        notificationService.sendStorageUpgradeEmail(user.getEmail(), additionalBytes);
//    }

    // Account Operations
    public void deleteAccount(Long userId) {
        User user = getUserById(userId);
        
        // Cascade delete files and folders
        fileRepository.deleteByOwner(user);
        folderRepository.deleteByOwner(user);
        
        userRepository.delete(user);
        notificationService.sendGoodbyeEmail(user.getEmail());
    }

    // Helper Methods
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Transactional
    public void updateStorageUsed(Long userId, long delta) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        long newUsage = user.getUsedStorage() + delta;
        user.setUsedStorage(Math.max(newUsage, 0));  // prevent negative storage
        userRepository.save(user);
    }

    // Internal usage by other services
    void verifyStorageAvailability(Long userId, long requiredSpace) {
        StorageInfoDto storage = getStorageInfo(userId);
        if (storage.usedBytes() + requiredSpace > storage.totalQuota()) {
            throw new StorageQuotaExceededException(
                "Require: " + requiredSpace + " bytes, Available: " + 
                (storage.totalQuota() - storage.usedBytes()) + " bytes"
            );
        }
    }
}