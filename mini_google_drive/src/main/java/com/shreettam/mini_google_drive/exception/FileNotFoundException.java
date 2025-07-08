package com.shreettam.mini_google_drive.exception;

@SuppressWarnings("serial")
public class FileNotFoundException extends ResourceNotFoundException {
    public FileNotFoundException(Long fileId) {
        super("File", fileId);
    }
}
