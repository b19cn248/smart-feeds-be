package com.olh.feeds.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private String titleEn;
    private String contentEn;
    private String url;
    private LocalDateTime timestamp;
    private String notificationType;
    private Long senderId;
    private Long recipientId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 