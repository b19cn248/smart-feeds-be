// SourceRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {

    Optional<Source> findByUrl(String url);

    boolean existsByUrl(String url);
}