package com.shreettam.mini_google_drive.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
