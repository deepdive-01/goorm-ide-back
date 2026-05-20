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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimerServiceTest {

    @Mock
    private TimerRepository timerRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private TimerService timerService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ROOM_ID = 10L;

    // ── startTimer ────────────────────────────────────────────

    @Test
    @DisplayName("존재하지 않는 room_id로 타이머 시작 시 ROOM_NOT_FOUND 예외가 발생한다")
    void startTimer_roomNotFound() {
        // Given
        given(spaceRepository.existsById(TEST_ROOM_ID)).willReturn(false);

        TimerStartRequest request = new TimerStartRequest(TEST_ROOM_ID, 1800);

        // When & Then
        assertThatThrownBy(() -> timerService.startTimer(TEST_USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);

        verify(timerRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 실행 중인 타이머가 있을 때 타이머 시작 시 TIMER_ALREADY_RUNNING 예외가 발생한다")
    void startTimer_alreadyRunning() {
        // Given
        given(spaceRepository.existsById(TEST_ROOM_ID)).willReturn(true);
        given(timerRepository.existsBySpaceIdAndStatus(TEST_ROOM_ID, TimerStatus.RUNNING)).willReturn(true);

        TimerStartRequest request = new TimerStartRequest(TEST_ROOM_ID, 1800);

        // When & Then
        assertThatThrownBy(() -> timerService.startTimer(TEST_USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TIMER_ALREADY_RUNNING);

        verify(timerRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 요청 시 타이머가 생성되고 RUNNING 상태로 반환된다")
    void startTimer_success() {
        // Given
        given(spaceRepository.existsById(TEST_ROOM_ID)).willReturn(true);
        given(timerRepository.existsBySpaceIdAndStatus(TEST_ROOM_ID, TimerStatus.RUNNING)).willReturn(false);

        Timer savedTimer = mock(Timer.class);
        given(savedTimer.getId()).willReturn(1L);
        given(savedTimer.getSpaceId()).willReturn(TEST_ROOM_ID);
        given(savedTimer.getDurationSeconds()).willReturn(1800);
        given(savedTimer.getStartedAt()).willReturn(LocalDateTime.now());
        given(savedTimer.getExpiresAt()).willReturn(LocalDateTime.now().plusSeconds(1800));
        given(savedTimer.getStatus()).willReturn(TimerStatus.RUNNING);
        given(timerRepository.save(any(Timer.class))).willReturn(savedTimer);

        TimerStartRequest request = new TimerStartRequest(TEST_ROOM_ID, 1800);

        // When
        TimerStartResponse result = timerService.startTimer(TEST_USER_ID, request);

        // Then
        assertThat(result.status()).isEqualTo(TimerStatus.RUNNING);
        assertThat(result.roomId()).isEqualTo(TEST_ROOM_ID);
        verify(timerRepository).save(any(Timer.class));
    }

    // ── stopTimer ─────────────────────────────────────────────

    @Test
    @DisplayName("실행 중인 타이머가 없을 때 타이머 종료 시 TIMER_NOT_FOUND 예외가 발생한다")
    void stopTimer_timerNotFound() {
        // Given
        given(timerRepository.findBySpaceIdAndStatus(TEST_ROOM_ID, TimerStatus.RUNNING))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> timerService.stopTimer(TEST_USER_ID, TEST_ROOM_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TIMER_NOT_FOUND);
    }

    @Test
    @DisplayName("정상 요청 시 타이머가 STOPPED 상태로 변경된다")
    void stopTimer_success() {
        // Given
        Timer timer = mock(Timer.class);
        given(timer.getId()).willReturn(1L);
        given(timer.getStatus()).willReturn(TimerStatus.STOPPED);
        given(timerRepository.findBySpaceIdAndStatus(TEST_ROOM_ID, TimerStatus.RUNNING))
                .willReturn(Optional.of(timer));

        // When
        TimerStopResponse result = timerService.stopTimer(TEST_USER_ID, TEST_ROOM_ID);

        // Then
        verify(timer).stop();
        assertThat(result.status()).isEqualTo(TimerStatus.STOPPED);
    }

    // ── getTimer ──────────────────────────────────────────────

    @Test
    @DisplayName("해당 룸에 타이머가 없을 때 TIMER_NOT_FOUND 예외가 발생한다")
    void getTimer_notFound() {
        // Given
        given(timerRepository.findTopBySpaceIdOrderByCreatedAtDesc(TEST_ROOM_ID))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> timerService.getTimer(TEST_ROOM_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TIMER_NOT_FOUND);
    }

    @Test
    @DisplayName("RUNNING 타이머 조회 시 remaining_seconds가 0 이상으로 반환된다")
    void getTimer_running_success() {
        // Given
        Timer timer = mock(Timer.class);
        given(timer.getId()).willReturn(1L);
        given(timer.getDurationSeconds()).willReturn(1800);
        given(timer.getStatus()).willReturn(TimerStatus.RUNNING);
        given(timer.getStartedAt()).willReturn(LocalDateTime.now().minusSeconds(900));
        given(timer.getExpiresAt()).willReturn(LocalDateTime.now().plusSeconds(900));
        given(timerRepository.findTopBySpaceIdOrderByCreatedAtDesc(TEST_ROOM_ID))
                .willReturn(Optional.of(timer));

        // When
        TimerQueryResponse result = timerService.getTimer(TEST_ROOM_ID);

        // Then
        assertThat(result.remainingSeconds()).isGreaterThan(0);
        assertThat(result.status()).isEqualTo(TimerStatus.RUNNING);
    }

    @Test
    @DisplayName("만료 시간이 지난 RUNNING 타이머 조회 시 EXPIRED 상태로 자동 전환된다")
    void getTimer_autoExpire() {
        // Given
        Timer timer = mock(Timer.class);
        given(timer.getId()).willReturn(1L);
        given(timer.getDurationSeconds()).willReturn(1800);
        given(timer.getStatus()).willReturn(TimerStatus.RUNNING);
        given(timer.getStartedAt()).willReturn(LocalDateTime.now().minusSeconds(2000));
        given(timer.getExpiresAt()).willReturn(LocalDateTime.now().minusSeconds(200));
        given(timerRepository.findTopBySpaceIdOrderByCreatedAtDesc(TEST_ROOM_ID))
                .willReturn(Optional.of(timer));

        // When
        timerService.getTimer(TEST_ROOM_ID);

        // Then: 만료 시간이 지났으므로 expire() 호출 확인
        verify(timer).expire();
    }
}
