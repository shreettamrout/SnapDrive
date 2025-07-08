package com.shreettam.mini_google_drive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    @Column(nullable = false)
    private Long size;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    // Default constructor
    public File() {}

    // Full constructor for file upload
    public File(String name, String s3Key, String mimeType, Long size, Boolean isPublic,
            User owner, Folder folder) {
    this.name = name;
    this.s3Key = s3Key;
    this.mimeType = mimeType;
    this.size = size;
    this.isPublic = isPublic != null ? isPublic : false;
    this.owner = owner;
    this.folder = folder;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
    
    }


    // Getters and Setters
    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getS3Key() { return s3Key; }

    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public Long getSize() { return size; }

    public void setSize(Long size) { this.size = size; }

    public String getMimeType() { return mimeType; }

    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Boolean getIsPublic() { return isPublic; }

    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }


    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getOwner() { return owner; }

    public void setOwner(User owner) { this.owner = owner; }

    public Folder getFolder() { return folder; }

    public void setFolder(Folder folder) { this.folder = folder; }

    // Update timestamp automatically
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Utility
    public String getFileExtension() {
        int dotIndex = name.lastIndexOf(".");
        return dotIndex != -1 ? name.substring(dotIndex + 1) : "";
    }
}
