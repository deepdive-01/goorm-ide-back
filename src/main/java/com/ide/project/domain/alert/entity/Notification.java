package com.ide.project.domain.alert.entity;


import com.ide.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 리스너가 있어야, 엔티티의 저장/수정 이벤트를 감지하고 시간을 채워줄 수 있음
public class Notification {

    // 알림 ID
    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 늘려줌
    private Long id;

    // 알림을 받는 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private NotificationType type;

    @Column(length = 255, nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(User receiver, User sender, NotificationType type, String content) {
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    public void markAsRead() {
        this.isRead = true;
    }






}
