package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.ForbiddenException;
import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.Notification;
import com.olh.feeds.dao.entity.User;
import com.olh.feeds.dao.repository.NotificationRepository;
import com.olh.feeds.dao.repository.UserRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.notification.NotificationResponse;
import com.olh.feeds.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final AuditorAware<String> auditorAware;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotificationsForCurrentUser(Pageable pageable) {
        log.info("Getting notifications for current user");

        // Get current user
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Get notifications
        Page<Notification> notificationsPage = notificationRepository.findByRecipientIdAndIsDeletedFalse(
                user.getId(), pageable);

        // Map to response
        Page<NotificationResponse> responsePage = notificationsPage.map(notification ->
                NotificationResponse.builder()
                        .id(notification.getId())
                        .title(notification.getTitle())
                        .content(notification.getContent())
                        .titleEn(notification.getTitleEn())
                        .contentEn(notification.getContentEn())
                        .url(notification.getUrl())
                        .timestamp(notification.getTimestamp())
                        .notificationType(notification.getNotificationType())
                        .senderId(notification.getSenderId())
                        .recipientId(notification.getRecipientId())
                        .isRead(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .updatedAt(notification.getUpdatedAt())
                        .build());

        return pageMapper.toPageDto(responsePage);
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long id) {
        log.info("Marking notification ID: {} as read", id);

        // Get current user
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Get notification
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "notification"));

        // Check if notification belongs to current user
        if (!notification.getRecipientId().equals(user.getId())) {
            log.error("User {} does not have access to notification {}", username, id);
            throw new ForbiddenException("notification.access.denied");
        }

        // Mark as read
        notification.setRead(true);
        notificationRepository.save(notification);

        log.info("Notification marked as read successfully");
    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead() {
        log.info("Marking all notifications as read for current user");

        // Get current user
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Mark all as read
        notificationRepository.markAllAsRead(user.getId());

        log.info("All notifications marked as read successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadNotificationsForCurrentUser() {
        log.info("Counting unread notifications for current user");

        // Get current user
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Count unread notifications
        return notificationRepository.countByRecipientIdAndIsReadFalseAndIsDeletedFalse(user.getId());
    }
} 