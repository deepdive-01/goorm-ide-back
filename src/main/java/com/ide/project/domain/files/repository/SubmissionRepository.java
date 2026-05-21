package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByStudentIdAndProblemId(Long studentId, Long problemId);

    List<Submission> findByProblemId(Long problemId);

    List<Submission> findByProblemIdAndStatus(Long problemId, String status);

    @Query(value = "SELECT COUNT(*) FROM feedbacks WHERE submission_id = :submissionId", nativeQuery = true)
    long countFeedbacksBySubmissionId(@Param("submissionId") Long submissionId);
}