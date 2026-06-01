package com.ide.project.domain.alert.dto.response;

import com.ide.project.domain.alert.entity.Notification;
import com.ide.project.domain.alert.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationCreateResponse(
        Long id,
        Long receiverId,
        Long senderId,
        NotificationType type,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationCreateResponse from(Notification notification) {
        return new NotificationCreateResponse(
                notification.getId(),
                notification.getReceiver().getId(),
                notification.getSender() != null ? notification.getSender().getId() : null,
                notification.getType(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
