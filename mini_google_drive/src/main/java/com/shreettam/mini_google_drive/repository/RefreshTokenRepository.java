package com.shreettam.mini_google_drive.repository;

import com.shreettam.mini_google_drive.model.RefreshToken;
import com.shreettam.mini_google_drive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    Optional<RefreshToken> findByUser_Id(Long userId);  
}
