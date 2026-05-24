package com.ide.project.domain.space.dto.response;

import java.time.LocalDateTime;

public record SpaceListItemResponse (
        Long id,
        String name,
        String description,
        int memberCount,
        boolean isActive,
        LocalDateTime createdAt

) {}
