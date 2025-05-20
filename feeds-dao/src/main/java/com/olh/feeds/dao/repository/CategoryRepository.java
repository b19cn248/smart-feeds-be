package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
        SELECT c FROM Category c 
        WHERE c.name = :name AND c.isDeleted = false
        """)
    Optional<Category> findByName(@Param("name") String name);

    @Query("""
        SELECT c FROM Category c 
        WHERE c.name IN :names AND c.isDeleted = false
        """)
    List<Category> findAllByNameIn(@Param("names") List<String> names);
}