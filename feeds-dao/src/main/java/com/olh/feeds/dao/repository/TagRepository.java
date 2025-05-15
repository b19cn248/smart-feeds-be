package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<Tag> findByName(String name);

  List<Tag> findAllByNameIn(Set<String> names);
}