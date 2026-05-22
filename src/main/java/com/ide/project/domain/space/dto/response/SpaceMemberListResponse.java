package com.ide.project.domain.space.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SpaceMemberListResponse(
        Long spaceId,
        String spaceName,
        int memberCount,
        List<MemberInfo> members
) {
    public record MemberInfo(
            Long userId,
            String nickname,
            String profileImageUrl,
            LocalDateTime joinedAt
    ) {}
}
