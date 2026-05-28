package com.ide.project.domain.space.dto.response;

import java.time.LocalDateTime;

public record SpaceUpdateResponse(
        Long id,
        String name,
        String description,
        LocalDateTime updatedAt
) {}
