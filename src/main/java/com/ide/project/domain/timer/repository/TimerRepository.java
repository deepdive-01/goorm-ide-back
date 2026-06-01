package com.ide.project.domain.timer.repository;

import com.ide.project.domain.timer.entity.Timer;
import com.ide.project.domain.timer.entity.TimerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimerRepository extends JpaRepository<Timer, Long> {
    boolean existsBySpaceIdAndStatus(Long spaceId, TimerStatus status);
    Optional<Timer> findBySpaceIdAndStatus(Long spaceId, TimerStatus status);
    Optional<Timer> findTopBySpaceIdOrderByCreatedAtDesc(Long spaceId);
}
