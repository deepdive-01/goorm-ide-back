package com.ide.project.domain.alert.repository;

import com.ide.project.domain.alert.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 전체 목록 조회 (is_read 필터링 없이, 전체 목록을 반환) -> receiverId가 받은 전체 알림을 조회
    Page<Notification> findByReceiverId(Long receiverId, Pageable pageable);

    // is_read 필터로 목록을 조회 -> receiverId가 받은 isRead 상태가 false인 값만 조회
    Page<Notification> findByReceiverIdAndIsRead(Long receiverId, boolean isRead, Pageable pageable);

    // 안 읽은 알림 수 (목록 응답의 unread_count에 사용) -> receiverId가 받은 알림 중 isRead가 false인 값의 갯수를 카운트
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // 단건 조회 -> 알림 ID로 알림을 찾지만, receiverId를 검사해 받는이가 맞는지 확인
    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);

    // 전체 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);



}
