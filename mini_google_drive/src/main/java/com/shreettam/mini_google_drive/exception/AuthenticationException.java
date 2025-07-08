package com.shreettam.mini_google_drive.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class AuthenticationException extends ApiException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
