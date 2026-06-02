package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // 특정 문제 + 특정 유저의 제출 조회 (upsert용)
    Optional<Submission> findByProblemIdAndUserId(Long problemId, Long userId);

    // 특정 문제의 모든 제출 조회
    List<Submission> findByProblemId(Long problemId);

    // 특정 유저의 전체 제출 조회
    List<Submission> findByUserId(Long userId);

    // 특정 문제의 상태별 제출 조회
    List<Submission> findByProblemIdAndStatus(Long problemId, String status);

    @Query(value = "SELECT COUNT(*) FROM feedbacks WHERE submission_id = :submissionId", nativeQuery = true)
    long countFeedbacksBySubmissionId(@Param("submissionId") Long submissionId);
}