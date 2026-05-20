package com.ide.project.domain.alert.dto.response;

import com.ide.project.domain.alert.entity.Notification;
import com.ide.project.domain.alert.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationItemResponse (
   Long id,
   NotificationType type,
   String content,
   boolean isRead,
   LocalDateTime createdAt
) {
    public static NotificationItemResponse from(Notification notification) {
        return new NotificationItemResponse(
                notification.getId(),
                notification.getType(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
