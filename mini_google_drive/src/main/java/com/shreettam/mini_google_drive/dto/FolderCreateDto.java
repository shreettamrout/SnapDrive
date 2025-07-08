package com.shreettam.mini_google_drive.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderCreateDto(
	    @NotBlank String name,
	    Long parentId
	) {}
