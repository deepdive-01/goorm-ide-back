package com.ide.project.domain.timer.service;

import com.ide.project.domain.space.repository.SpaceRepository;
import com.ide.project.domain.timer.dto.request.TimerStartRequest;
import com.ide.project.domain.timer.dto.response.TimerQueryResponse;
import com.ide.project.domain.timer.dto.response.TimerStartResponse;
import com.ide.project.domain.timer.dto.response.TimerStopResponse;
import com.ide.project.domain.timer.entity.Timer;
import com.ide.project.domain.timer.entity.TimerStatus;
import com.ide.project.domain.timer.repository.TimerRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimerService {

    private final TimerRepository timerRepository;
    private final SpaceRepository spaceRepository;

    @Transactional
    public TimerStartResponse startTimer(Long userId, TimerStartRequest request) {
        if (!spaceRepository.existsById(request.roomId())) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        if (timerRepository.existsBySpaceIdAndStatus(request.roomId(), TimerStatus.RUNNING)) {
            throw new BusinessException(ErrorCode.TIMER_ALREADY_RUNNING);
        }

        LocalDateTime now = LocalDateTime.now();
        Timer timer = Timer.builder()
                .spaceId(request.roomId())
                .createdBy(userId)
                .durationSeconds(request.durationSeconds())
                .startedAt(now)
                .expiresAt(now.plusSeconds(request.durationSeconds()))
                .build();

        return TimerStartResponse.from(timerRepository.save(timer));
    }

    @Transactional
    public TimerStopResponse stopTimer(Long userId, Long roomId) {
        Timer timer = timerRepository.findBySpaceIdAndStatus(roomId, TimerStatus.RUNNING)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIMER_NOT_FOUND));

        LocalDateTime stoppedAt = LocalDateTime.now();
        timer.stop();
        return TimerStopResponse.of(timer, stoppedAt);
    }

    @Transactional
    public TimerQueryResponse getTimer(Long roomId) {
        Timer timer = timerRepository.findTopBySpaceIdOrderByCreatedAtDesc(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIMER_NOT_FOUND));

        if (timer.getStatus() == TimerStatus.RUNNING && LocalDateTime.now().isAfter(timer.getExpiresAt())) {
            timer.expire();
        }

        return TimerQueryResponse.from(timer);
    }
}
