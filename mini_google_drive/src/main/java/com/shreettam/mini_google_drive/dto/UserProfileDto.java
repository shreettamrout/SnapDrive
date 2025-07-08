package com.shreettam.mini_google_drive.dto;

import java.time.LocalDateTime;

public record UserProfileDto(
		Long id,
		String name,
		String email,
		LocalDateTime createdAt,
	    long storageQuota,
	    long storageUsed
		) {}

