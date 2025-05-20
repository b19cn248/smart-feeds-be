package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dto.response.source.SourceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {

    Optional<Source> findByUrl(String url);

    boolean existsByUrl(String url);

    @Query("SELECT new com.olh.feeds.dto.response.source.SourceResponse(" +
            "s.id, s.url, s.language, s.type, s.accountId, s.hashtag, " +
            "s.userId, s.active, s.createdAt) " +
            "FROM Source s " +
            "WHERE (:active IS NULL OR s.active = :active) " +
            "AND s.isDeleted = false " +
            "ORDER BY s.createdAt DESC")
    Page<SourceResponse> findAllSources(
            @Param("active") Boolean active,
            Pageable pageable);

    @Query("SELECT new com.olh.feeds.dto.response.source.SourceResponse(" +
            "s.id, s.url, s.language, s.type, s.accountId, s.hashtag, " +
            "s.userId, s.active, s.createdAt) " +
            "FROM Source s " +
            "WHERE s.id = :sourceId " +
            "AND s.isDeleted = false")
    SourceResponse findSourceById(@Param("sourceId") Long sourceId);

    @Query("SELECT s FROM Source s WHERE s.url = :url AND s.isDeleted = false")
    List<Source> findAllByUrl(@Param("url") String url);

}