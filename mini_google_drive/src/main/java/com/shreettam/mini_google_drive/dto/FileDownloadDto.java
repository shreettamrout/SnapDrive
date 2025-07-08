package com.shreettam.mini_google_drive.dto;

public record FileDownloadDto(
    String fileName,
    String mimeType,
    byte[] data
) {}

