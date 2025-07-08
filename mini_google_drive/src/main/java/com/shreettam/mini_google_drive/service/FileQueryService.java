package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.model.File;
import com.shreettam.mini_google_drive.repository.FileRepository;
import com.shreettam.mini_google_drive.exception.FileNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FileQueryService {

    private final FileRepository fileRepository;

    public FileQueryService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }
}
