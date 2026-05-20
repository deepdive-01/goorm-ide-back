package com.ide.project.domain.timer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "timers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Timer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private TimerStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Timer(Long spaceId, Long createdBy, int durationSeconds, LocalDateTime startedAt, LocalDateTime expiresAt) {
        this.spaceId = spaceId;
        this.createdBy = createdBy;
        this.durationSeconds = durationSeconds;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.status = TimerStatus.RUNNING;
    }

    public void stop() {
        this.status = TimerStatus.STOPPED;
    }

    public void expire() {
        this.status = TimerStatus.EXPIRED;
    }
}
