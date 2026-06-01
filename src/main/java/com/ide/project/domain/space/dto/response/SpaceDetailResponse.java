package com.ide.project.domain.space.dto.response;

import java.time.LocalDateTime;

public record SpaceDetailResponse (
        Long id,
        String name,
        String description,
        MentorInfo mentor,
        String inviteCode,
        int memberCount,
        boolean isPublic,
        boolean isActive,
        LocalDateTime createdAt
) {
    public record MentorInfo(Long id, String nickname) {}
}
