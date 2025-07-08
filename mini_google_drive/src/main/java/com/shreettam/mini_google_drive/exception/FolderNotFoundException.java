package com.shreettam.mini_google_drive.exception;

@SuppressWarnings("serial")
public class FolderNotFoundException extends RuntimeException {
 public FolderNotFoundException() {
     super("Folder not found.");
 }

 public FolderNotFoundException(Long folderId) {
     super("Folder not found with ID: " + folderId);
 }
}

