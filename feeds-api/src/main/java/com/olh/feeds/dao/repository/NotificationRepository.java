package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Find notifications by recipient ID and not deleted
     *
     * @param recipientId ID of the recipient
     * @param pageable pagination information
     * @return page of notifications
     */
    Page<Notification> findByRecipientIdAndIsDeletedFalse(Long recipientId, Pageable pageable);

    /**
     * Mark all notifications as read for a user
     *
     * @param recipientId ID of the recipient
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.isDeleted = false")
    void markAllAsRead(Long recipientId);

    /**
     * Count unread notifications for a user
     *
     * @param recipientId ID of the recipient
     * @return number of unread notifications
     */
    Long countByRecipientIdAndIsReadFalseAndIsDeletedFalse(Long recipientId);
} 