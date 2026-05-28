package com.ide.project.domain.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackUpdateRequest(

        @NotBlank
        @Size(max = 1000)
        String content

) {}
