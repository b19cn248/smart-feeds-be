package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;

    @Column(name = "points")
    private Integer points = 0;

    @Column(name = "settings_id")
    private Long settingsId;

    @Column(name = "read_later_list_id")
    private Long readLaterListId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "keycloak_id")
    private String keycloakId;
}