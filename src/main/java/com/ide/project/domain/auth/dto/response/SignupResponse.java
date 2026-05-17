package com.ide.project.domain.auth.dto.response;

import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;

import java.time.LocalDateTime;

public record SignupResponse (
        Long id,
        String email,
        String name,
        String nickname,
        Role role,
        LocalDateTime createdAt
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
