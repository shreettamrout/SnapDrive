package com.shreettam.mini_google_drive.exception;

@SuppressWarnings("serial")
public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long userId) {
        super("User", userId);
    }
    
    public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
    }
}
