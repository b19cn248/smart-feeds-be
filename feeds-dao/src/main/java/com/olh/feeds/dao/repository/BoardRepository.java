// feeds-dao/src/main/java/com/olh/feeds/dao/repository/BoardRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Board;
import com.olh.feeds.dto.response.board.BoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.board.BoardResponse(
            b.id, b.name, b.description, b.color, b.icon, b.isPublic, b.createdAt
        )
        FROM Board b
        WHERE b.createdBy = :username
        AND b.isDeleted = false
        ORDER BY b.createdAt DESC
        """)
    Page<BoardResponse> findBoardsByUsername(
            @Param("username") String username,
            Pageable pageable
    );

    @Query("""
        SELECT b
        FROM Board b
        WHERE b.id = :id
        AND b.isDeleted = false
        """)
    Board findBoardById(@Param("id") Long id);
}