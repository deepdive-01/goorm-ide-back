package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // 특정 유저가 특정 문제에 대해 제출한 기록 단건 조회
    Optional<Submission> findByProblemIdAndUserId(Long problemId, Long userId);
}