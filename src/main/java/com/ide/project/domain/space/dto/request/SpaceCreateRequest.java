package com.ide.project.domain.space.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpaceCreateRequest (
    @NotBlank
    @Size(max = 100)
    String name,

    String description
) {}
