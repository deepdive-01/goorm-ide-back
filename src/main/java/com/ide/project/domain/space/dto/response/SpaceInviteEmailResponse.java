package com.ide.project.domain.space.dto.response;

public record SpaceInviteEmailResponse(
        Long spaceId,
        String spaceName,
        int sentCount
) {}
