package com.shreettam.mini_google_drive.dto;


public record FolderRequestDto(
        String name,
        Long parentId
) {}
