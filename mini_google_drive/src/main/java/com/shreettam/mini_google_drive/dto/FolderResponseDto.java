package com.shreettam.mini_google_drive.dto;


import java.time.LocalDateTime;
import java.util.List;

public record FolderResponseDto(
    Long id,
    String name,
    String path,
    LocalDateTime createdAt,
    Long parentId,
    List<FileResponseDto> files,
    List<FolderMinimalDto> subfolders
) {}
