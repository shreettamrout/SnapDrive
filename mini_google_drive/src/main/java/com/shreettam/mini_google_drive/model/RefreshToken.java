package com.shreettam.mini_google_drive.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    public Long getId() { return id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public Instant getExpiryDate() { return expiryDate; }

    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}
