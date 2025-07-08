package com.shreettam.mini_google_drive.service;

import com.shreettam.mini_google_drive.exception.*;
import com.shreettam.mini_google_drive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.Duration;


@Service
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-expiration-sec}")
    private Long refreshTokenDurationSec;

    private static final String PREFIX = "refresh_token:";

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate,
                               UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    public String createRefreshToken(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(PREFIX + userId, token, Duration.ofSeconds(refreshTokenDurationSec));
        return token;
    }


    public void verifyToken(Long userId, String token) {
        String stored = redisTemplate.opsForValue().get(PREFIX + userId);
        if (stored == null || !stored.equals(token)) {
            throw new TokenRefreshException(token, "Invalid or expired refresh token");
        }
    }

    public void revokeToken(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    public String getToken(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }
}
