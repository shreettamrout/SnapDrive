package com.shreettam.mini_google_drive.dto;


import java.time.LocalDateTime;

public class SharedAccessDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String permission;
    private LocalDateTime sharedAt;

    // Constructors
    public SharedAccessDto(Long id, Long userId, String userName, String userEmail, String permission, LocalDateTime sharedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.permission = permission;
        this.sharedAt = sharedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
}
