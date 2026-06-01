package com.ide.project.domain.timer.dto.response;

import com.ide.project.domain.timer.entity.Timer;
import com.ide.project.domain.timer.entity.TimerStatus;

import java.time.LocalDateTime;

public record TimerStopResponse(
        Long timerId,
        TimerStatus status,
        LocalDateTime stoppedAt
) {
    public static TimerStopResponse of(Timer timer, LocalDateTime stoppedAt) {
        return new TimerStopResponse(
                timer.getId(),
                timer.getStatus(),
                stoppedAt
        );
    }
}
