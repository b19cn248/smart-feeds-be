package com.olh.feeds.service;

import com.olh.feeds.dto.response.notification.NotificationDto;
import java.util.List;

public interface WebSocketService {
    void sendNotification(List<String> usernames, NotificationDto notification);
    void sendLogoutNotification(List<String> usernames, NotificationDto notification);
} 