package com.ide.project.domain.workspace.dto.response;

import java.util.List;

public record ParticipantEventMessage(
        Long spaceId,
        String type,
        Long userId,
        String nickname,
        List<String> participants
) {}
