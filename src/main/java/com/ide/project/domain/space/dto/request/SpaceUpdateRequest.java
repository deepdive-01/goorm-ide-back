package com.ide.project.domain.space.dto.request;

import jakarta.validation.constraints.Size;

public record SpaceUpdateRequest (
        @Size(max = 100)
        String name,
        String description,
        Boolean isActive
) {}
