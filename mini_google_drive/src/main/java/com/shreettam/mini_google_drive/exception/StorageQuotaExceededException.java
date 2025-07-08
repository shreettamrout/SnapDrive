package com.shreettam.mini_google_drive.exception;


/**
 * Thrown when a user attempts to upload a file that would exceed their storage quota.
 */
@SuppressWarnings("serial")
public class StorageQuotaExceededException extends RuntimeException {

    public StorageQuotaExceededException() {
        super("Storage quota exceeded");
    }

    public StorageQuotaExceededException(String message) {
        super(message);
    }

    public StorageQuotaExceededException(long requiredBytes, long availableBytes) {
        super(String.format(
            "Storage quota exceeded. Required: %d bytes, Available: %d bytes",
            requiredBytes,
            availableBytes
        ));
    }

    public StorageQuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
