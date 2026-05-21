package com.ide.project.domain.alert.dto.response;

import java.util.List;

public record NotificationListResponse (
        List<NotificationItemResponse> notifications,
        int page,
        int size,
        long totalCount,
        long unreadCount
) {}
