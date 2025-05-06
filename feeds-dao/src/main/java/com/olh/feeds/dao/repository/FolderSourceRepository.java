// feeds-dao/src/main/java/com/olh/feeds/dao/repository/FolderSourceRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.FolderSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderSourceRepository extends JpaRepository<FolderSource, Long> {

    @Query("SELECT fs FROM FolderSource fs WHERE fs.folderId = :folderId AND fs.isDeleted = false")
    List<FolderSource> findByFolderId(@Param("folderId") Long folderId);

    boolean existsByFolderIdAndSourceId(Long folderId, Long sourceId);
}