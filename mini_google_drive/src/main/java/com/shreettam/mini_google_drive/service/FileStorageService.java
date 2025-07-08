package com.shreettam.mini_google_drive.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
	 String upload(MultipartFile file, String fileKey);
	    byte[] download(String fileKey);
	    void delete(String fileKey);
	    String getPublicUrl(String fileKey);
	    String initiateMultipartUpload(String filename, String contentType);

}
