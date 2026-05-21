package com.ide.project.domain.timer.dto.response;

import com.ide.project.domain.timer.entity.Timer;
import com.ide.project.domain.timer.entity.TimerStatus;

import java.time.LocalDateTime;

public record TimerStartResponse(
        Long timerId,
        Long roomId,
        int durationSeconds,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        TimerStatus status
) {
    public static TimerStartResponse from(Timer timer) {
        return new TimerStartResponse(
                timer.getId(),
                timer.getSpaceId(),
                timer.getDurationSeconds(),
                timer.getStartedAt(),
                timer.getExpiresAt(),
                timer.getStatus()
        );
    }
}
