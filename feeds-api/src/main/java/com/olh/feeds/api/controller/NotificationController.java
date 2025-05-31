package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.notification.NotificationDto;
import com.olh.feeds.dto.response.notification.NotificationResponse;
import com.olh.feeds.service.NotificationService;
import com.olh.feeds.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final WebSocketService webSocketService;
    private final NotificationService notificationService;

    /**
     * Get notifications for current user
     */
    @GetMapping
    public ResponseGeneral<PageResponse<NotificationResponse>> getNotifications(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get notifications for current user");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "notification.list.success",
                notificationService.getNotificationsForCurrentUser(pageable)
        );
    }

    /**
     * Mark a notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseGeneral<Void> markNotificationAsRead(
            @PathVariable("id") Long id
    ) {
        log.info("REST request to mark notification ID: {} as read", id);
        notificationService.markNotificationAsRead(id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "notification.mark.read.success"
        );
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseGeneral<Void> markAllNotificationsAsRead() {
        log.info("REST request to mark all notifications as read");
        notificationService.markAllNotificationsAsRead();
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "notification.mark.all.read.success"
        );
    }

    /**
     * Send notification
     */
    @PostMapping("/send")
    public ResponseGeneral<Void> sendNotification(
            @RequestBody NotificationDto notification,
            @RequestParam List<String> usernames
    ) {
        log.info("REST request to send notification to users: {}", usernames);
        webSocketService.sendNotification(usernames, notification);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "notification.send.success"
        );
    }

    /**
     * Send logout notification
     */
    @PostMapping("/send/logout")
    public ResponseGeneral<Void> sendLogoutNotification(
            @RequestBody NotificationDto notification,
            @RequestParam List<String> usernames
    ) {
        log.info("REST request to send logout notification to users: {}", usernames);
        webSocketService.sendLogoutNotification(usernames, notification);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "notification.logout.send.success"
        );
    }

    /**
     * Count unread notifications for current user
     */
    @GetMapping("/count/unread")
    public ResponseGeneral<Long> countUnreadNotifications() {
        log.info("REST request to count unread notifications for current user");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "notification.count.unread.success",
                notificationService.countUnreadNotificationsForCurrentUser()
        );
    }
} 