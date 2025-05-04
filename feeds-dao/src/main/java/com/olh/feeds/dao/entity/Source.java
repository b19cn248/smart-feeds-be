package com.olh.feeds.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Source extends BaseEntity {

    @Column(name = "url", nullable = false, length = 512)
    private String url;

    @Column(name = "language")
    private String language;

    @Column(name = "type")
    private String type;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "hashtag")
    private String hashtag;

    @Column(name = "category")
    private String category;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "active")
    private Boolean active = true;
}