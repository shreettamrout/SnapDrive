package com.shreettam.mini_google_drive.dto;

import jakarta.validation.constraints.Positive;

public record QuotaUpdateDto(
    @Positive long additionalBytes
) {}