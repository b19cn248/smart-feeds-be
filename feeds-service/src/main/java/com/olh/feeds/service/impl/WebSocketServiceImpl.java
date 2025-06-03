package com.olh.feeds.service.impl;

import com.olh.feeds.dao.entity.Notification;
import com.olh.feeds.dao.repository.NotificationRepository;
import com.olh.feeds.dto.response.notification.NotificationDto;
import com.olh.feeds.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void sendNotification(List<String> usernames, NotificationDto notification) {
        log.info("Sending notification to users: {}", usernames);

        // Save notification to database
        Notification notificationEntity = Notification.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .titleEn(notification.getTitleEn())
                .contentEn(notification.getContentEn())
                .url(notification.getUrl())
                .timestamp(notification.getTimestamp())
                .notificationType(notification.getNotificationType())
                .senderId(notification.getSenderId())
                .recipientId(notification.getRecipientId())
                .isRead(false)
                .isDeleted(false)
                .build();

        notificationRepository.save(notificationEntity);
        log.info("Saved notification to database with ID: {}", notificationEntity.getId());

        // Send via WebSocket
        for (String username : usernames) {
            String destination = "/topic/notifications/" + username;
            messagingTemplate.convertAndSend(destination, notification);
            log.info("Sent notification to user: {}", username);
        }
    }

    @Override
    @Transactional
    public void sendLogoutNotification(List<String> usernames, NotificationDto notification) {
        log.info("Sending logout notification to users: {}", usernames);

        // Save notification to database
        Notification notificationEntity = Notification.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .titleEn(notification.getTitleEn())
                .contentEn(notification.getContentEn())
                .url(notification.getUrl())
                .timestamp(notification.getTimestamp())
                .notificationType(notification.getNotificationType())
                .senderId(notification.getSenderId())
                .recipientId(notification.getRecipientId())
                .isRead(false)
                .isDeleted(false)
                .build();

        notificationRepository.save(notificationEntity);
        log.info("Saved logout notification to database with ID: {}", notificationEntity.getId());

        // Send via WebSocket
        for (String username : usernames) {
            String destination = "/topic/notifications/" + username;
            messagingTemplate.convertAndSend(destination, notification);
            log.info("Sent logout notification to user: {}", username);
        }
    }
} 