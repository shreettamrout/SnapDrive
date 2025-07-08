package com.shreettam.mini_google_drive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.shreettam.mini_google_drive.security.JwtUtils;

@Configuration
public class JwtConfig {
    
    @Value("${app.jwt-secret}")
    private String jwtSecret;
    
    @Value("${app.jwt-expiration}")
    private int jwtExpirationMs;
    
    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecret, jwtExpirationMs);
    }
    
    
}
