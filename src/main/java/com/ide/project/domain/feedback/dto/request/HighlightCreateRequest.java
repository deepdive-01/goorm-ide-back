package com.ide.project.domain.feedback.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HighlightCreateRequest(

        @NotNull
        @JsonProperty("submission_id")
        Long submissionId,

        @NotNull
        @JsonProperty("start_line")
        Integer startLine,

        @NotNull
        @JsonProperty("end_line")
        Integer endLine,

        @Size(max = 1000)
        String content

) {}
