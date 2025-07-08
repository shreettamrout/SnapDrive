package com.shreettam.mini_google_drive.dto;

public record RefreshTokenRequestDto(
		Long userId,
	    String refreshToken
	) {}
