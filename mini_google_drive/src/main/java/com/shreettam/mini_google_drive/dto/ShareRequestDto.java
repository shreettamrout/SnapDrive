package com.shreettam.mini_google_drive.dto;

import com.shreettam.mini_google_drive.model.SharedAccess;

public record ShareRequestDto(
	    Long fileId,
	    Long userId,
	    SharedAccess.PermissionType permission
	) {}

