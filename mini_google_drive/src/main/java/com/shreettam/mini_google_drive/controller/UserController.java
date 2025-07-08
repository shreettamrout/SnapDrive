package com.shreettam.mini_google_drive.controller;

import com.shreettam.mini_google_drive.dto.UserProfileDto;
import com.shreettam.mini_google_drive.model.User;
import com.shreettam.mini_google_drive.service.AuthService;
import com.shreettam.mini_google_drive.service.StorageQuotaService;
import com.shreettam.mini_google_drive.exception.AuthenticationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;
    private final StorageQuotaService storageQuotaService;

    public UserController(AuthService authService, StorageQuotaService storageQuotaService) {
        this.authService = authService;
        this.storageQuotaService = storageQuotaService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getUserProfile() {
        try {
            User user = authService.getCurrentUser();
            long used = storageQuotaService.getStorageUsed(user.getId());
            long quota = storageQuotaService.getStorageQuota(user.getId());

            return ResponseEntity.ok(new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                quota,
                used
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();  // Unauthorized
        }
    }

}
