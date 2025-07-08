package com.shreettam.mini_google_drive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDto(
	    @NotBlank String name,
	    @NotBlank @Email String email,
	    @Size(min = 8, max = 100) String password
	) {}
