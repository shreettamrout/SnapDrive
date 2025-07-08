package com.shreettam.mini_google_drive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
	    @NotBlank @Email String email,
	    @NotBlank String password
	) {}
