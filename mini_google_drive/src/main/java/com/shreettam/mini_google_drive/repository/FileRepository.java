package com.shreettam.mini_google_drive.repository;

import com.shreettam.mini_google_drive.model.File;
import com.shreettam.mini_google_drive.model.Folder;
import com.shreettam.mini_google_drive.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    // Basic queries
    Optional<File> findByS3Key(String s3Key);
    List<File> findByOwner(User owner);
    Page<File> findByOwnerId(Long ownerId, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM File f WHERE f.owner = :owner")
    void deleteByOwner(@Param("owner") User owner);

    // Folder-based queries
    List<File> findByOwnerAndFolder(User owner, Folder folder);
    List<File> findByFolderId(Long folderId);

    // Search functionality
    @Query("SELECT f FROM File f WHERE f.owner = :owner AND LOWER(f.name) LIKE LOWER(concat('%', :query, '%'))")
    List<File> searchByOwnerAndName(@Param("owner") User owner, @Param("query") String query);

    // Storage calculations
    @Query("SELECT SUM(f.size) FROM File f WHERE f.owner.id = :ownerId")
    Long calculateTotalStorageUsed(@Param("ownerId") Long ownerId);

    // Bulk operations
    @Modifying
    @Query("UPDATE File f SET f.folder = null WHERE f.folder.id = :folderId")
    void detachFilesFromFolder(@Param("folderId") Long folderId);

    // Native query for complex operations
    @Modifying
    @Query(value = "DELETE FROM files WHERE owner_id = :ownerId AND created_at < NOW() - INTERVAL '30 days'", 
           nativeQuery = true)
    void deleteOldFiles(@Param("ownerId") Long ownerId);

    // Permission checks
    @Query("SELECT f FROM File f JOIN SharedAccess s ON f.id = s.file.id WHERE s.user.id = :userId")
    List<File> findSharedFiles(@Param("userId") Long userId);

    // Permission checks
    Optional<File> findByIdAndOwnerId(Long fileId, Long ownerId); // ✅ Corrected
}
