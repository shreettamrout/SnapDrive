package com.shreettam.mini_google_drive.dto;

import java.time.LocalDateTime;

public record FileResponseDto(
    Long id,
    String name,
    String url,
    Long size,
    String mimeType,
    LocalDateTime createdAt,
    Long folderId
) {}
