package com.ide.project.domain.feedback.repository;

import com.ide.project.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findBySubmissionIdOrderByCreatedAtAsc(Long submissionId);
}
