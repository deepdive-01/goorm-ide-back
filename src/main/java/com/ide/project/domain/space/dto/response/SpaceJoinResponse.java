package com.ide.project.domain.space.dto.response;

import java.time.LocalDateTime;

public record SpaceJoinResponse(
        Long spaceId,
        String spaceName,
        LocalDateTime joinedAt
) {
}
