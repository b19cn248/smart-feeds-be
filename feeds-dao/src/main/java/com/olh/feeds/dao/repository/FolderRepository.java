// feeds-dao/src/main/java/com/olh/feeds/dao/repository/FolderRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Folder;
import com.olh.feeds.dto.response.folder.FolderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    @Query("SELECT new com.olh.feeds.dto.response.folder.FolderResponse(" +
            "f.id, f.name, f.theme, f.userId, f.createdAt) " +
            "FROM Folder f " +
            "WHERE (:userId IS NULL OR f.userId = :userId) " +
            "AND f.isDeleted = false " +
            "ORDER BY f.createdAt DESC")
    Page<FolderResponse> findAllFolders(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("SELECT f FROM Folder f WHERE f.id = :id AND f.isDeleted = false")
    Folder findFolderById(@Param("id") Long id);
}