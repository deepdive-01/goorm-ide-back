package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByProblemIdAndUserId(Long problemId, Long userId);
}