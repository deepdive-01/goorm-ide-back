package com.ide.project.domain.feedback.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(

        @NotNull
        @JsonProperty("submission_id")
        Long submissionId,

        @NotBlank
        @Size(max = 1000)
        String content

) {}
