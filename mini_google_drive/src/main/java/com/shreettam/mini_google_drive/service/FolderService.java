package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.*;
import com.shreettam.mini_google_drive.model.*;
import com.shreettam.mini_google_drive.repository.*;

import jakarta.mail.FolderNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserService userService;
    private final FileService fileService;

    public FolderService(FolderRepository folderRepository,
                         FileRepository fileRepository,
                         UserService userService,
                         FileService fileService) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userService = userService;
        this.fileService = fileService;
    }

    //Create Folder
    public FolderResponseDto createFolder(FolderCreateDto dto, Long ownerId) throws FolderNotFoundException {
        // Step 1: Get the owner (user) by ID
        User owner = userService.getUserById(ownerId);

        // Step 2: Fetch parent folder if parentId is provided
        Folder parent = null;
        if (dto.parentId() != null) {
            parent = folderRepository.findById(dto.parentId())
                .filter(f -> f.getOwner().getId().equals(ownerId))
                .orElseThrow(FolderNotFoundException::new);
;
        }

        // Step 3: Build the folder path
        String path = (parent != null)
            ? parent.getPath() + parent.getName() + "/"
            : "/";

        // Step 4: Create and save the new Folder entity
        Folder newFolder = new Folder(
            dto.name(),
            path,
            owner,
            parent
        );
        Folder savedFolder = folderRepository.save(newFolder);

        // Step 5: Return DTO response
        return mapToDto(savedFolder);
    }


    public FolderResponseDto getFolderContents(Long folderId, Long requesterId) throws FolderNotFoundException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException());

        if (!folder.getOwner().getId().equals(requesterId)) {
            throw new AccessDeniedException("You don't own this folder");
        }

        return mapToDto(folder); // mapToDto now includes files and subfolders
    }


    public FolderResponseDto moveFolder(Long folderId, Long newParentId, Long ownerId) throws FolderNotFoundException {
        Folder folder = folderRepository.findById(folderId)
                .filter(f -> f.getOwner().getId().equals(ownerId))
                .orElseThrow(() -> new AccessDeniedException("Folder not owned by user"));

        Folder newParent = null;
        if (newParentId != null) {
            newParent = folderRepository.findById(newParentId)
                    .filter(f -> f.getOwner().getId().equals(ownerId))
                    .orElseThrow(() -> new FolderNotFoundException());
        }

        if (newParent != null && isAncestor(folder, newParent)) {
            throw new InvalidOperationException("Cannot move folder to its own subfolder");
        }

        folder.setParent(newParent);
        updateFolderPath(folder);
        Folder updated = folderRepository.save(folder);
        return mapToDto(updated);
    }

    public void deleteFolder(Long folderId, Long ownerId) {
        Folder folder = folderRepository.findById(folderId)
                .filter(f -> f.getOwner().getId().equals(ownerId))
                .orElseThrow(() -> new AccessDeniedException("Folder not owned by user"));

        fileRepository.findByFolderId(folderId).forEach(file ->
                fileService.deleteFile(file.getId(), ownerId)
        );

        folderRepository.findByParentId(folderId).forEach(subfolder ->
                deleteFolder(subfolder.getId(), ownerId)
        );

        folderRepository.delete(folder);
    }

    private void updateFolderPath(Folder folder) {
        String newPath = folder.getParent() != null ?
                folder.getParent().getPath() + folder.getParent().getName() + "/" : "/";

        folder.setPath(newPath);

        folderRepository.findByParentId(folder.getId()).forEach(this::updateFolderPath);
    }

    private boolean isAncestor(Folder source, Folder target) {
        if (target.getParent() == null) return false;
        if (target.getParent().equals(source)) return true;
        return isAncestor(source, target.getParent());
    }

    private FolderResponseDto mapToDto(Folder folder) {
        List<FileResponseDto> files = fileRepository.findByFolderId(folder.getId()).stream()
                .map(fileService::mapToDto)
                .collect(Collectors.toList());

        List<FolderMinimalDto> subfolders = folderRepository.findByParentId(folder.getId()).stream()
                .map(this::mapToMinimalDto)
                .collect(Collectors.toList());

        return new FolderResponseDto(
                folder.getId(),
                folder.getName(),
                folder.getPath(),
                folder.getCreatedAt(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                files,
                subfolders
        );
    }


    private FolderMinimalDto mapToMinimalDto(Folder folder) {
        return new FolderMinimalDto(
                folder.getId(),
                folder.getName()
        );
    }
}