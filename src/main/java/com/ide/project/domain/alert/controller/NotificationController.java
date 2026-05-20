package com.ide.project.domain.alert.controller;

import com.ide.project.domain.alert.dto.response.NotificationListResponse;
import com.ide.project.domain.alert.entity.Notification;
import com.ide.project.domain.alert.service.NotificationService;
import com.ide.project.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        Long userId = getCurrentUserId();
    }



}
