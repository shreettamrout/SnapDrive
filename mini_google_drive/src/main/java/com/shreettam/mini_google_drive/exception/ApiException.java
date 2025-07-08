package com.shreettam.mini_google_drive.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
