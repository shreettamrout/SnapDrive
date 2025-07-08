package com.shreettam.mini_google_drive.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
@Table(name = "shared_access",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"file_id", "user_id"})
       })
public class SharedAccess {

    @EmbeddedId
    private SharedAccessId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 10)
    private PermissionType permissionType;

    @Column(name = "shared_at", nullable = false)
    private LocalDateTime sharedAt = LocalDateTime.now();

    // Embeddable composite key
    @SuppressWarnings("serial")
    @Embeddable
    public static class SharedAccessId implements java.io.Serializable {
        private Long fileId;
        private Long userId;

        // Required no-arg constructor
        public SharedAccessId() {}

        // All-args constructor
        public SharedAccessId(Long fileId, Long userId) {
            this.fileId = fileId;
            this.userId = userId;
        }

        // Getters
        public Long getFileId() { return fileId; }
        public Long getUserId() { return userId; }

        // Setters (required for JPA)
        public void setFileId(Long fileId) { this.fileId = fileId; }
        public void setUserId(Long userId) { this.userId = userId; }

        // Must override equals() and hashCode()
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SharedAccessId)) return false;
            SharedAccessId that = (SharedAccessId) o;
            return Objects.equals(fileId, that.fileId) && 
                   Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileId, userId);
        }
    }
    // Permission types
    public enum PermissionType {
        READ, 
        WRITE
    }

    // Constructors
    public SharedAccess() {}

    public SharedAccess(File file, User user, PermissionType permissionType) {
        this.file = file;
        this.user = user;
        this.permissionType = permissionType;
        this.id = new SharedAccessId(file.getId(), user.getId());
    }

    // Getters and Setters
    public SharedAccessId getId() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    // Utility Methods
    public boolean isWritePermission() {
        return permissionType == PermissionType.WRITE;
    }
}
