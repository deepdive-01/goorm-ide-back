package com.ide.project.domain.files.dto;

import com.ide.project.domain.files.entity.Submission;
import java.time.LocalDateTime;

public record StudentSubmissionResponse(
    Long id,
    Long problemId,
    Long studentId,
    String savedCode,
    String submittedCode,
    String status,
    Integer executionTimeMs,
    Integer executionMemoryKb,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static StudentSubmissionResponse from(Submission submission) {
        return new StudentSubmissionResponse(
            submission.getId(),
            submission.getProblemId(),
            submission.getUserId(),
            submission.getSavedCode(),
            submission.getSubmittedCode(),
            submission.getStatus(),
            submission.getExecutionTimeMs(),
            submission.getExecutionMemoryKb(),
            submission.getErrorMessage(),
            submission.getCreatedAt(),
            submission.getUpdatedAt()
        );
    }
}