package com.shreettam.mini_google_drive.controller;

import com.shreettam.mini_google_drive.dto.*;
import com.shreettam.mini_google_drive.exception.TokenRefreshException;
import com.shreettam.mini_google_drive.model.User;
import com.shreettam.mini_google_drive.service.AuthService;
import jakarta.mail.AuthenticationFailedException;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid UserRegistrationDto request) {
        AuthResponseDto response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@RequestBody AuthRequestDto authRequest) throws AuthenticationFailedException {
        AuthResponseDto response = authService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        try {
            AuthResponseDto response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(403).body(null); // Forbidden
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RefreshTokenRequestDto request) {

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.badRequest().body("Missing Bearer token");

        String accessToken = authHeader.substring(7); // strip "Bearer "
        authService.logout(request.userId(), accessToken);

        return ResponseEntity.ok("Logged out and tokens revoked");
    }


    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() throws AuthenticationFailedException {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
