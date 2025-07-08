package com.shreettam.mini_google_drive.dto;

public record StorageInfoDto(
	    long usedBytes,
	    long totalQuota,
	    double percentageUsed
	) {}
