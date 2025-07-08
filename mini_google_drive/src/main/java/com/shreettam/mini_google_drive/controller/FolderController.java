package com.shreettam.mini_google_drive.controller;

import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.AuthenticationException;
import com.shreettam.mini_google_drive.model.User;
import com.shreettam.mini_google_drive.service.AuthService;
import com.shreettam.mini_google_drive.service.FolderService;

import jakarta.mail.FolderNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;
    private final AuthService authService;

    public FolderController(FolderService folderService, AuthService authService) {
        this.folderService = folderService;
        this.authService = authService;
    }

    @PostMapping("/folder")
    public ResponseEntity<?> createFolder(@RequestBody FolderCreateDto dto) {
        try {
            User user = authService.getCurrentUser();
            FolderResponseDto created = folderService.createFolder(dto, user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (FolderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parent folder not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Folder creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFolderContents(@PathVariable Long id) {
        try {
            User user = authService.getCurrentUser();
            FolderResponseDto folder = folderService.getFolderContents(id, user.getId());
            return ResponseEntity.ok(folder);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (FolderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get folder contents: " + e.getMessage());
        }
    }

    @PutMapping("/move/{id}")
    public ResponseEntity<?> moveFolder(@PathVariable Long id, @RequestParam(required = false) Long newParentId) {
        try {
            User user = authService.getCurrentUser();
            FolderResponseDto updated = folderService.moveFolder(id, newParentId, user.getId());
            return ResponseEntity.ok(updated);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (FolderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to move folder: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable Long id) {
        try {
            User user = authService.getCurrentUser();
            folderService.deleteFolder(id, user.getId());
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete folder: " + e.getMessage());
        }
    }
}
