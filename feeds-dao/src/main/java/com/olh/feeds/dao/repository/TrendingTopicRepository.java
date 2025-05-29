package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TrendingTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrendingTopicRepository extends JpaRepository<TrendingTopic, Long> {

    @Query("""
        SELECT tt FROM TrendingTopic tt
        WHERE tt.topicName = :name
        AND :date BETWEEN tt.startDate AND tt.endDate
        AND tt.isDeleted = false
        ORDER BY tt.score DESC
        """)
    Optional<TrendingTopic> findByNameAndDate(
            @Param("name") String name,
            @Param("date") LocalDate date);

    @Query("""
        SELECT tt FROM TrendingTopic tt
        WHERE :date BETWEEN tt.startDate AND tt.endDate
        AND tt.isDeleted = false
        ORDER BY tt.score DESC
        """)
    Page<TrendingTopic> findActiveTopics(
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("""
        SELECT tt FROM TrendingTopic tt
        WHERE tt.categoryId = :categoryId
        AND :date BETWEEN tt.startDate AND tt.endDate
        AND tt.isDeleted = false
        ORDER BY tt.score DESC
        """)
    Page<TrendingTopic> findActiveByCategoryId(
            @Param("categoryId") Long categoryId,
            @Param("date") LocalDate date,
            Pageable pageable);
}