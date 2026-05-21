package com.ide.project.domain.alert.service;

import com.ide.project.domain.alert.dto.request.NotificationCreateRequest;
import com.ide.project.domain.alert.dto.response.NotificationCreateResponse;
import com.ide.project.domain.alert.dto.response.NotificationListResponse;
import com.ide.project.domain.alert.dto.response.NotificationReadAllResponse;
import com.ide.project.domain.alert.entity.Notification;
import com.ide.project.domain.alert.entity.NotificationType;
import com.ide.project.domain.alert.repository.NotificationRepository;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private NotificationService notificationService;

    private static final Long RECEIVER_ID = 1L;
    private static final Long SENDER_ID = 2L;
    private static final Long NOTIFICATION_ID = 10L;

    // ===================== getNotifications =====================

    @Test
    @DisplayName("isRead 파라미터 없이 조회하면 전체 알림 목록을 반환한다")
    void getNotifications_allNotifications() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(notificationRepository.findByReceiverId(RECEIVER_ID, pageable)).willReturn(page);
        given(notificationRepository.countByReceiverIdAndIsReadFalse(RECEIVER_ID)).willReturn(3L);

        // When
        NotificationListResponse response = notificationService.getNotifications(RECEIVER_ID, null, pageable);

        // Then: 전체 조회 쿼리가 실행되어야 함
        verify(notificationRepository).findByReceiverId(RECEIVER_ID, pageable);
        verify(notificationRepository, never()).findByReceiverIdAndIsRead(any(), anyBoolean(), any());
        assertThat(response.unreadCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("isRead=false로 조회하면 읽지 않은 알림만 반환한다")
    void getNotifications_unreadOnly() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(notificationRepository.findByReceiverIdAndIsRead(RECEIVER_ID, false, pageable)).willReturn(page);
        given(notificationRepository.countByReceiverIdAndIsReadFalse(RECEIVER_ID)).willReturn(2L);

        // When
        NotificationListResponse response = notificationService.getNotifications(RECEIVER_ID, false, pageable);

        // Then: isRead 필터 쿼리가 실행되어야 함
        verify(notificationRepository).findByReceiverIdAndIsRead(RECEIVER_ID, false, pageable);
        verify(notificationRepository, never()).findByReceiverId(any(), any());
        assertThat(response.unreadCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("isRead=true로 조회하면 읽은 알림만 반환한다")
    void getNotifications_readOnly() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(notificationRepository.findByReceiverIdAndIsRead(RECEIVER_ID, true, pageable)).willReturn(page);
        given(notificationRepository.countByReceiverIdAndIsReadFalse(RECEIVER_ID)).willReturn(0L);

        // When
        notificationService.getNotifications(RECEIVER_ID, true, pageable);

        // Then: isRead 필터 쿼리가 실행되어야 함
        verify(notificationRepository).findByReceiverIdAndIsRead(RECEIVER_ID, true, pageable);
        verify(notificationRepository, never()).findByReceiverId(any(), any());
    }

    // ===================== readNotification =====================

    @Test
    @DisplayName("존재하지 않는 알림 ID로 읽음 처리 요청 시 NOTIFICATION_NOT_FOUND 예외가 발생한다")
    void readNotification_notFound() {
        // Given
        given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.readNotification(NOTIFICATION_ID, RECEIVER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("본인 알림이 아닌 경우 읽음 처리 요청 시 FORBIDDEN 예외가 발생한다")
    void readNotification_forbidden() {
        User otherUser = mock(User.class);
        given(otherUser.getId()).willReturn(999L);

        Notification notification = mock(Notification.class);
        given(notification.getReceiver()).willReturn(otherUser);
        given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

        // When & Then
        assertThatThrownBy(() -> notificationService.readNotification(NOTIFICATION_ID, RECEIVER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(notification, never()).markAsRead();
    }

    @Test
    @DisplayName("정상적인 읽음 처리 요청 시 markAsRead가 호출된다")
    void readNotification_success() {
        // Given
        User receiver = mock(User.class);
        given(receiver.getId()).willReturn(RECEIVER_ID);

        Notification notification = mock(Notification.class);
        given(notification.getReceiver()).willReturn(receiver);
        given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

        // When
        notificationService.readNotification(NOTIFICATION_ID, RECEIVER_ID);

        // Then
        verify(notification).markAsRead();
    }

    // ===================== readAllNotifications =====================

    @Test
    @DisplayName("전체 읽음 처리 요청 시 업데이트된 알림 수를 반환한다")
    void readAllNotifications_success() {
        // Given
        given(notificationRepository.markAllAsReadByReceiverId(RECEIVER_ID)).willReturn(5);

        // When
        NotificationReadAllResponse response = notificationService.readAllNotifications(RECEIVER_ID);

        // Then
        assertThat(response.updateCount()).isEqualTo(5);
        verify(notificationRepository).markAllAsReadByReceiverId(RECEIVER_ID);
    }

    // ===================== createNotification =====================

    @Test
    @DisplayName("존재하지 않는 sender ID로 알림 생성 요청 시 USER_NOT_FOUND 예외가 발생한다")
    void createNotification_senderNotFound() {
        // Given
        NotificationCreateRequest request = new NotificationCreateRequest(RECEIVER_ID, NotificationType.SYSTEM, "테스트 알림");
        given(userRepository.findById(SENDER_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.createNotification(request, SENDER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 receiver ID로 알림 생성 요청 시 USER_NOT_FOUND 예외가 발생한다")
    void createNotification_receiverNotFound() {
        // Given
        NotificationCreateRequest request = new NotificationCreateRequest(RECEIVER_ID, NotificationType.SYSTEM, "테스트 알림");
        given(userRepository.findById(SENDER_ID)).willReturn(Optional.of(mock(User.class)));
        given(userRepository.findById(RECEIVER_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.createNotification(request, SENDER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("정상적인 알림 생성 요청 시 알림이 저장되고 응답 DTO를 반환한다")
    void createNotification_success() {
        // Given
        NotificationCreateRequest request = new NotificationCreateRequest(RECEIVER_ID, NotificationType.SYSTEM, "테스트 알림");

        User sender = mock(User.class);
        given(sender.getId()).willReturn(SENDER_ID);

        User receiver = mock(User.class);
        given(receiver.getId()).willReturn(RECEIVER_ID);

        given(userRepository.findById(SENDER_ID)).willReturn(Optional.of(sender));
        given(userRepository.findById(RECEIVER_ID)).willReturn(Optional.of(receiver));

        Notification savedNotification = mock(Notification.class);
        given(savedNotification.getId()).willReturn(NOTIFICATION_ID);
        given(savedNotification.getReceiver()).willReturn(receiver);
        given(savedNotification.getSender()).willReturn(sender);
        given(savedNotification.getType()).willReturn(NotificationType.SYSTEM);
        given(savedNotification.getContent()).willReturn("테스트 알림");
        given(savedNotification.isRead()).willReturn(false);
        given(notificationRepository.save(any(Notification.class))).willReturn(savedNotification);

        // When
        NotificationCreateResponse response = notificationService.createNotification(request, SENDER_ID);

        // Then
        assertThat(response.id()).isEqualTo(NOTIFICATION_ID);
        assertThat(response.receiverId()).isEqualTo(RECEIVER_ID);
        assertThat(response.senderId()).isEqualTo(SENDER_ID);
        assertThat(response.type()).isEqualTo(NotificationType.SYSTEM);
        assertThat(response.content()).isEqualTo("테스트 알림");
        assertThat(response.isRead()).isFalse();
        verify(notificationRepository).save(any(Notification.class));
    }
}
