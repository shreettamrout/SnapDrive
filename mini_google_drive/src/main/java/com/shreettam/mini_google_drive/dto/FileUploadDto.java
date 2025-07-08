package com.shreettam.mini_google_drive.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;


public record FileUploadDto(
    @NotNull MultipartFile file,
    Long folderId,
    Boolean isPublic
) {}
