package com.olh.feeds.dto.request.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Username không được để trống")
    private String username;

    private String name;
    private String location;
    private String preferences;
    private Integer points = 0;
    private Long settingsId;
    private Long readLaterListId;
    private Long subscriptionId;
    private String keycloakId; // ID từ Keycloak để theo dõi
    private String createdBy;
    private String updatedBy;
}
