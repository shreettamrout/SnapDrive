package com.shreettam.mini_google_drive.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateDto(
	    String name,
	    @Size(min = 8, max = 100) String password
	) {}
