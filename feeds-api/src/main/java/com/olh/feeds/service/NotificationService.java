package com.olh.feeds.service;

import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.notification.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    /**
     * Get notifications for current user
     *
     * @param pageable pagination information
     * @return page of notifications
     */
    PageResponse<NotificationResponse> getNotificationsForCurrentUser(Pageable pageable);

    /**
     * Mark a notification as read
     *
     * @param id notification ID
     */
    void markNotificationAsRead(Long id);

    /**
     * Mark all notifications as read for current user
     */
    void markAllNotificationsAsRead();

    /**
     * Count unread notifications for current user
     *
     * @return number of unread notifications
     */
    Long countUnreadNotificationsForCurrentUser();
} 