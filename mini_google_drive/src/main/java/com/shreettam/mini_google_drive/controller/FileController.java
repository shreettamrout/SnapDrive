package com.shreettam.mini_google_drive.controller;

import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.AuthenticationException;
import com.shreettam.mini_google_drive.model.User;
import com.shreettam.mini_google_drive.service.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final AuthService authService;

    public FileController(FileService fileService,
                          SharedAccessService sharedAccessService,
                          AuthService authService) {
        this.fileService = fileService;
        this.authService = authService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "folderId", required = false) Long folderId,
                                    @RequestParam(value = "isPublic", required = false) Boolean isPublic) {
        try {
            User user = authService.getCurrentUser();
            FileResponseDto uploadedFile = fileService.uploadFile(file, folderId, user.getId(), isPublic);
            return ResponseEntity.ok(uploadedFile);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable Long id) {
        try {
            User user = authService.getCurrentUser();
            FileDownloadDto fileData = fileService.downloadFile(id, user.getId());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileData.mimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileData.fileName() + "\"")
                    .body(fileData.data());

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Download failed: " + e.getMessage());
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            User user = authService.getCurrentUser();
            fileService.deleteFile(id, user.getId());
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed: " + e.getMessage());
        }
    }

    @PostMapping("/share")
    public ResponseEntity<?> shareFile(@RequestBody ShareRequestDto request) {
        try {
            User user = authService.getCurrentUser();
            fileService.shareFile(request.fileId(), request.userId(), request.permission().name(), user.getId());
            return ResponseEntity.ok("File shared successfully");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Share failed: " + e.getMessage());
        }
    }

    
}
