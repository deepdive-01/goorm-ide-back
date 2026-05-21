package com.ide.project.domain.files.dto;

import com.ide.project.domain.files.entity.Problem;
import java.time.LocalDateTime;

public record ProblemResponse(
    Long id,
    String title,
    String description,
    String difficulty,
    String language,
    String starterCode,
    boolean isPublished,
    LocalDateTime createdAt
) {
    public static ProblemResponse from(Problem problem) {
        return new ProblemResponse(
            problem.getId(),
            problem.getTitle(),
            problem.getDescription(),
            problem.getDifficulty(),
            problem.getLanguage(),
            problem.getStarterCode(),
            problem.isPublished(),
            problem.getCreatedAt()
        );
    }
}