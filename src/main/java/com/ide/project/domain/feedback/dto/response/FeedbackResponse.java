package com.ide.project.domain.feedback.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ide.project.domain.feedback.entity.Feedback;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeedbackResponse(

        @JsonProperty("feedback_id")
        Long feedbackId,

        @JsonProperty("submission_id")
        Long submissionId,

        String type,

        @JsonProperty("start_line")
        Integer startLine,

        @JsonProperty("end_line")
        Integer endLine,

        String color,

        String content,

        @JsonProperty("created_by")
        String createdBy,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getSubmissionId(),
                feedback.getType().name(),
                feedback.getStartLine(),
                feedback.getEndLine(),
                feedback.getColor(),
                feedback.getContent(),
                feedback.getMentorNickname(),
                feedback.getCreatedAt()
        );
    }
}
