package com.shreettam.mini_google_drive.dto;

public record AuthResponseDto(
	    Long id,
	    String email,
	    String name,
	    String jwtToken,
	    String refreshToken, 
	    long storageQuota,
	    long storageUsed
	) {}
