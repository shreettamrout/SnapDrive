package com.shreettam.mini_google_drive.repository;


import com.shreettam.mini_google_drive.model.Folder;
import com.shreettam.mini_google_drive.model.User;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    // Basic queries
    Optional<Folder> findByNameAndOwner(String name, User owner);
    List<Folder> findByOwner(User owner);
    Page<Folder> findByOwnerId(Long ownerId, Pageable pageable);

    // Hierarchical queries
    List<Folder> findByParent(Folder parent);
    List<Folder> findByParentId(Long parentId);
    List<Folder> findByOwnerAndParent(User owner, Folder parent);
    
    @Modifying
    @Transactional  // Optional but good if used directly
    @Query("DELETE FROM Folder f WHERE f.owner = :owner")
    void deleteByOwner(@Param("owner") User owner);


    // Path-based queries
    @Query("SELECT f FROM Folder f WHERE f.owner = :owner AND f.path LIKE :pathPattern")
    List<Folder> findByOwnerAndPathLike(@Param("owner") User owner, 
                                      @Param("pathPattern") String pathPattern);

    // Recursive query (requires PostgreSQL)
    @Query(value = """
        WITH RECURSIVE folder_tree AS (
            SELECT * FROM folders WHERE id = :folderId
            UNION ALL
            SELECT f.* FROM folders f
            JOIN folder_tree ft ON f.parent_id = ft.id
        ) SELECT * FROM folder_tree
        """, nativeQuery = true)
    List<Folder> findFolderTree(@Param("folderId") Long folderId);

    // Storage calculations
    @Query("SELECT COUNT(f) FROM Folder f WHERE f.owner.id = :ownerId")
    Long countByOwner(@Param("ownerId") Long ownerId);

    // Bulk operations
    @Modifying
    @Query("UPDATE Folder f SET f.parent = :newParent WHERE f.parent = :oldParent")
    void reparentFolders(@Param("oldParent") Folder oldParent, 
                        @Param("newParent") Folder newParent);

    // Native query for cleanup
    @Modifying
    @Query(value = """
        DELETE FROM folders 
        WHERE owner_id = :ownerId 
        AND created_at < NOW() - INTERVAL '6 months'
        AND NOT EXISTS (SELECT 1 FROM files WHERE folder_id = folders.id)
        """, nativeQuery = true)
    int cleanupEmptyFolders(@Param("ownerId") Long ownerId);
	
}
