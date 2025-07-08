package com.shreettam.mini_google_drive.repository;


import com.shreettam.mini_google_drive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used for login)
    Optional<User> findByEmail(String email);

    // Check if email exists (for registration)
    boolean existsByEmail(String email);

    // Custom query: Update user's storage usage
    @Modifying
    @Query("UPDATE User u SET u.usedStorage = u.usedStorage + :delta WHERE u.id = :userId")
    void updateStorageUsed(@Param("userId") Long userId, @Param("delta") long delta);

    
    // Native query example: Reset password
    @Modifying
    @Query(value = "UPDATE users SET password = :newPassword WHERE id = :userId", nativeQuery = true)
    void resetPassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    @Query("SELECT COALESCE(SUM(f.size), 0) FROM File f WHERE f.owner.id = :userId")
    long calculateStorageUsed(@Param("userId") Long userId);

}
