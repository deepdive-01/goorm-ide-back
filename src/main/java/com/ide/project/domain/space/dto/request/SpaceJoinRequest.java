package com.ide.project.domain.space.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SpaceJoinRequest (
        @NotBlank
        String inviteCode
) {}
