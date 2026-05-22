package com.ide.project.domain.feedback.dto.response;

import com.ide.project.domain.feedback.entity.Feedback;

import java.time.LocalDateTime;

public record FeedbackResponse(

        Long feedback_id,
        Long submission_id,
        String type,
        String content,
        Integer start_line,
        Integer end_line,
        String color,
        String created_by,
        LocalDateTime created_at

) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getSubmissionId(),
                feedback.getType().name(),
                feedback.getContent(),
                feedback.getStartLine(),
                feedback.getEndLine(),
                feedback.getColor() != null ? feedback.getColor().name() : null,
                feedback.getMentor().getNickname(),
                feedback.getCreatedAt()
        );
    }
}
