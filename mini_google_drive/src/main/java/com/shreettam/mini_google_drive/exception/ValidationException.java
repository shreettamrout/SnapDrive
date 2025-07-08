package com.shreettam.mini_google_drive.exception;


import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class ValidationException extends ApiException {
    public ValidationException(List<FieldError> errors) {
        super(formatErrors(errors), HttpStatus.BAD_REQUEST);
    }

    private static String formatErrors(List<FieldError> errors) {
        return errors.stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
    }
}
