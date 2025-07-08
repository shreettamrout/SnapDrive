package com.shreettam.mini_google_drive.service;


import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.ResourceNotFoundException;
import com.shreettam.mini_google_drive.exception.AuthenticationException;
import com.shreettam.mini_google_drive.exception.EmailAlreadyExistsException;
import com.shreettam.mini_google_drive.model.*;
import com.shreettam.mini_google_drive.repository.*;
import com.shreettam.mini_google_drive.security.*;

import jakarta.mail.AuthenticationFailedException;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;


@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final RedisTemplate<String, String> redisTemplate;


    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService,
                       NotificationService notificationService,
                       RedisTemplate<String, String> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.notificationService = notificationService;
        this.redisTemplate = redisTemplate;
    }

    //  Authenticate user and return JWT
    public AuthResponseDto authenticate(AuthRequestDto request) throws AuthenticationFailedException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String jwt = jwtUtils.generateToken(user.getEmail());

            notificationService.sendLoginEmail(user.getEmail());

            //  Generate and store refresh token in Redis
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            return new AuthResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                jwt,
                refreshToken, // use String directly
                user.getStorageQuota(),
                userRepository.calculateStorageUsed(user.getId())
            );

        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    //  Refresh access token using refresh token
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        Long userId = request.userId();
        String refreshToken = request.refreshToken();
        refreshTokenService.verifyToken(userId, refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtUtils.generateToken(user.getEmail());
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);

        return new AuthResponseDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            newAccessToken,
            newRefreshToken,
            user.getStorageQuota(),
            userRepository.calculateStorageUsed(userId)
        );
    }

    //Logout
    public void logout(Long userId, String accessToken) {
        // Extract email from token and get user
        String emailFromToken = jwtUtils.extractUsername(accessToken);
        User user = userRepository.findByEmail(emailFromToken)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + emailFromToken));

        if (!user.getId().equals(userId)) {
            throw new AuthenticationException("Token-userId mismatch!");
        }

        // Revoke refresh token
        boolean refreshDeleted = Boolean.TRUE.equals(redisTemplate.delete("refresh_token:" + userId));
        System.out.println("[LOGOUT] Refresh token deleted: " + refreshDeleted);

        // Blacklist access token
        long tokenTTL = jwtUtils.getExpirationDuration(accessToken);
        if (tokenTTL > 0) {
            redisTemplate.opsForValue().set("blacklist:" + accessToken, "revoked", Duration.ofSeconds(tokenTTL));
            System.out.println("[LOGOUT] Access token blacklisted for " + tokenTTL + " seconds");
        } else {
            System.out.println("[LOGOUT] Access token already expired");
        }

        SecurityContextHolder.clearContext();
    }




    //  Get current authenticated user
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        }

        if (principal instanceof User user) {
            return user;
        }

        throw new AuthenticationException("Invalid principal: " + principal);
    }

    //  Register new user
    public AuthResponseDto registerUser(UserRegistrationDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStorageQuota(1024L * 1024L * 1024L); // 1 GB

        userRepository.save(user);

        notificationService.sendWelcomeEmail(user.getEmail());

        String jwt = jwtUtils.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponseDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            jwt,
            refreshToken,
            user.getStorageQuota(),
            0L
        );
    }
}
