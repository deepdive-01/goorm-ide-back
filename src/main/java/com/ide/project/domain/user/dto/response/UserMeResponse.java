package com.ide.project.domain.user.dto.response;

import java.time.LocalDateTime;

import com.ide.project.domain.user.entity.LoginType;
import com.ide.project.domain.user.entity.Role;

public record UserMeResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Role role,
        LoginType loginType,
        String profileImageUrl,
        LocalDateTime createdAt
) {
}