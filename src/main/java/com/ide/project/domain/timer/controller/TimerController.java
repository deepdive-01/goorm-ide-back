package com.ide.project.domain.timer.controller;

import com.ide.project.domain.timer.dto.request.TimerStartRequest;
import com.ide.project.domain.timer.dto.response.TimerQueryResponse;
import com.ide.project.domain.timer.dto.response.TimerStartResponse;
import com.ide.project.domain.timer.dto.response.TimerStopResponse;
import com.ide.project.domain.timer.service.TimerService;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timer")
@RequiredArgsConstructor
public class TimerController {

    private final TimerService timerService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TimerStartResponse>> startTimer(
            @Valid @RequestBody TimerStartRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireMentor(auth);
        Long userId = (Long) auth.getPrincipal();

        TimerStartResponse data = timerService.startTimer(userId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "TIMER_STARTED", "타이머가 시작되었습니다.", data));
    }

    @PostMapping("/{roomId}/stop")
    public ResponseEntity<ApiResponse<TimerStopResponse>> stopTimer(
            @PathVariable Long roomId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireMentor(auth);
        Long userId = (Long) auth.getPrincipal();

        TimerStopResponse data = timerService.stopTimer(userId, roomId);
        return ResponseEntity.ok(ApiResponse.success(200, "TIMER_STOPPED", "타이머가 종료되었습니다.", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<TimerQueryResponse>> getTimer(
            @RequestParam Long roomId
    ) {
        TimerQueryResponse data = timerService.getTimer(roomId);
        return ResponseEntity.ok(ApiResponse.success(200, "TIMER_FOUND", "타이머 정보를 조회했습니다.", data));
    }

    private void requireMentor(Authentication auth) {
        boolean isMentor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MENTOR"));
        if (!isMentor) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
