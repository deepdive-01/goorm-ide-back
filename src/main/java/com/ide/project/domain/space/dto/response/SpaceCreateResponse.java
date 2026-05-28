package com.ide.project.domain.space.dto.response;

import java.time.LocalDateTime;

public record SpaceCreateResponse (
        Long id,
        String name,
        String description,
        boolean isPublic,
        String inviteCode,
        boolean isActive,
        LocalDateTime createdAt

) {}
