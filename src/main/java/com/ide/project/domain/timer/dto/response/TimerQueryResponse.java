package com.ide.project.domain.timer.dto.response;

import com.ide.project.domain.timer.entity.Timer;
import com.ide.project.domain.timer.entity.TimerStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record TimerQueryResponse(
        Long timerId,
        int durationSeconds,
        long remainingSeconds,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        TimerStatus status
) {
    public static TimerQueryResponse from(Timer timer) {
        long remaining = 0;
        if (timer.getStatus() == TimerStatus.RUNNING) {
            remaining = Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(), timer.getExpiresAt()));
        }
        return new TimerQueryResponse(
                timer.getId(),
                timer.getDurationSeconds(),
                remaining,
                timer.getStartedAt(),
                timer.getExpiresAt(),
                timer.getStatus()
        );
    }
}
