package com.shreettam.mini_google_drive.repository;


import com.shreettam.mini_google_drive.model.File;
import com.shreettam.mini_google_drive.model.SharedAccess;
import com.shreettam.mini_google_drive.model.SharedAccess.PermissionType;
import com.shreettam.mini_google_drive.model.SharedAccess.SharedAccessId;
import com.shreettam.mini_google_drive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedAccessRepository extends JpaRepository<SharedAccess, SharedAccessId> {

    // Basic queries
    List<SharedAccess> findByFileId(Long fileId);
    List<SharedAccess> findByUserId(Long userId);
    
    // Permission checks
    @Query("SELECT sa FROM SharedAccess sa WHERE sa.file.id = :fileId AND sa.user.id = :userId")
    Optional<SharedAccess> findAccess(@Param("fileId") Long fileId, 
                                    @Param("userId") Long userId);

    // Complex queries
    @Query("SELECT sa FROM SharedAccess sa JOIN FETCH sa.file WHERE sa.user = :user AND sa.permissionType = 'WRITE'")
    List<SharedAccess> findWritableFiles(@Param("user") User user);

    // Bulk operations
    @Modifying
    @Query("DELETE FROM SharedAccess sa WHERE sa.file.id = :fileId")
    void revokeAllAccess(@Param("fileId") Long fileId);

    @Modifying
    @Query("UPDATE SharedAccess sa SET sa.permissionType = :newPermission " +
           "WHERE sa.file.id = :fileId AND sa.user.id = :userId")
    int updatePermission(@Param("fileId") Long fileId,
                        @Param("userId") Long userId,
                        @Param("newPermission") SharedAccess.PermissionType newPermission);

    // Native query for performance
    @Modifying
    @Query(value = """
        DELETE FROM shared_access 
        USING files 
        WHERE shared_access.file_id = files.id 
        AND files.owner_id = :ownerId
        """, nativeQuery = true)
    int revokeAllByOwner(@Param("ownerId") Long ownerId);
	boolean existsByFileAndUser(File file, User recipient);
	Optional<SharedAccess> findById(SharedAccessId id);
	@Modifying
	@Query("DELETE FROM SharedAccess sa WHERE sa.file.id = :fileId")
	void deleteByFileId(@Param("fileId") Long fileId);
	boolean existsByFileIdAndUserIdAndPermissionType(Long fileId, Long userId, PermissionType permissionType);



}
