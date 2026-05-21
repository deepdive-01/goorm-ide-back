package com.ide.project.domain.alert.controller;

import com.ide.project.domain.alert.dto.request.NotificationCreateRequest;
import com.ide.project.domain.alert.dto.response.NotificationCreateResponse;
import com.ide.project.domain.alert.dto.response.NotificationListResponse;
import com.ide.project.domain.alert.dto.response.NotificationReadAllResponse;
import com.ide.project.domain.alert.service.NotificationService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // GET /api/v1/notification - 알림 목록 조회
    // is_read 파라미터가 없으면 전체, true/false에 따라 읽은 알림, 안읽음 알림 조회
    @Operation(summary = "알림 목록 조회", description = "isRead 파라미터가 없으면 전체 알림을 조회합니다. true면 읽은 알림, false면 안 읽은 알림만 조회합니다.", security =
    @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        Long userId = getCurrentUserId();
        NotificationListResponse response = notificationService.getNotifications(userId, isRead, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "알림 목록을 조회했습니다.", response));
    }

    // PATCH /api/v1/notification/read-all - 알림 전체 읽음 처리
    @Operation(summary = "알림 전체 읽음 처리", description = "로그인한 사용자의 읽지 않은 알림을 모두 읽음 처리합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<NotificationReadAllResponse>> readAllNotifications() {
        Long userId = getCurrentUserId();
        NotificationReadAllResponse response = notificationService.readAllNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "NOTIFICATION_ALL_READ_SUCCESS", "모든 알림을 읽음 처리했습니다.", response));
    }

    // PATCH /api/v1/notifications/{id}/read - 알림 단건 읽음 처리
    @Operation(summary = "알림 단건 읽음 처리", description = "알림 ID로 특정 알림을 읽음 처리합니다. 본인 알림만 처리할 수 있습니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId();
        notificationService.readNotification(id, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "NOTIFICATION_READ_SUCCESS", "알림을 읽음 처리했습니다."));
    }

    // POST /api/v1/notification - 알림 생성
    @Operation(summary = "알림 생성", description = "특정 유저에게 알림을 생성합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationCreateResponse>> createNotification(
            @Valid
            @RequestBody NotificationCreateRequest request
    ) {
        Long senderId = getCurrentUserId();
        NotificationCreateResponse response = notificationService.createNotification(request, senderId);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "NOTIFICATION_CREATE_SUCCESS", "알림이 생성됐습니다.", response));
    }

    // user 찾기
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }



}
