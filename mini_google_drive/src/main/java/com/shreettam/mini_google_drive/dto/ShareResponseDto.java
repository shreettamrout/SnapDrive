package com.shreettam.mini_google_drive.dto;

import java.time.LocalDateTime;

public record ShareResponseDto(
    Long fileId,
    String fileName,
    String ownerName,
    String permission,
    LocalDateTime sharedAt
) {}
