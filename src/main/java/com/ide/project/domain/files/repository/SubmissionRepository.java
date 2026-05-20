package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // 특정 학생이 특정 문제에 이미 제출한 기록이 있는지 조회
    Optional<Submission> findByStudentIdAndProblemId(Long studentId, Long problemId);
}