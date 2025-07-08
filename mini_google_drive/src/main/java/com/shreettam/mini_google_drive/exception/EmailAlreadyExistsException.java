package com.shreettam.mini_google_drive.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " is already registered", HttpStatus.CONFLICT);
    }
}
