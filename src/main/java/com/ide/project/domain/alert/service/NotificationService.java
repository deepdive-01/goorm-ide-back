package com.ide.project.domain.alert.service;

import com.ide.project.domain.alert.dto.request.NotificationCreateRequest;
import com.ide.project.domain.alert.dto.response.NotificationCreateResponse;
import com.ide.project.domain.alert.dto.response.NotificationItemResponse;
import com.ide.project.domain.alert.dto.response.NotificationListResponse;
import com.ide.project.domain.alert.dto.response.NotificationReadAllResponse;
import com.ide.project.domain.alert.entity.Notification;
import com.ide.project.domain.alert.repository.NotificationRepository;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 알림 목록 조회
    public NotificationListResponse getNotifications(Long userId, Boolean isRead, Pageable pageable) {

        // isRead 파라미터 유무에 따라 다른 쿼리를 실행하도록
        Page<Notification> page = (isRead != null) ?
                notificationRepository.findByReceiverIdAndIsRead(userId, isRead, pageable)
                : notificationRepository.findByReceiverId(userId, pageable);

        List<NotificationItemResponse> notification = page.getContent().stream()
                .map(NotificationItemResponse::from)
                .toList();

        long unreadCount = notificationRepository.countByReceiverIdAndIsReadFalse(userId);

        return new NotificationListResponse(
                notification,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(), // 전체
                unreadCount
        );
    }

    // 알림 단건 읽음 처리
    @Transactional
    public void readNotification(Long notificationId, Long userId) {

        // 알림이 존재하는지 확인
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 본인 알림이 아닐경우 403을 반환
        if (!notification.getReceiver().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 알림 읽음 처리
        notification.markAsRead();
    }


    // 알림 전체 읽음 처리
    @Transactional
    public NotificationReadAllResponse readAllNotifications(Long userId) {
        int updatedCount = notificationRepository.markAllAsReadByReceiverId(userId);
        return new NotificationReadAllResponse(updatedCount);
    }

    // 알림 생성
    @Transactional
    public NotificationCreateResponse createNotification(NotificationCreateRequest request, Long senderId) {

        // 보내는 사람 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 받는 사람 조회
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 알림을 생성
        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(request.type())
                .content(request.content())
                .build();

        // 생성한 알림을 response DTO에 담고, 레포에 저장
        return NotificationCreateResponse.from(notificationRepository.save(notification));
    }

}
