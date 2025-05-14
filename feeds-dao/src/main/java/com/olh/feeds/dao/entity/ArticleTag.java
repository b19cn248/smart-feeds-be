package com.olh.feeds.dao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "article_id")
  private Long articleId;

  @Column(name = "tag_id")
  private Long tagId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "is_deleted")
  private Boolean isDeleted;
}