package com.ide.project.domain.alert.dto.request;


import com.ide.project.domain.alert.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationCreateRequest (
        @NotNull Long receiverId,
        @NotNull NotificationType type,
        @NotBlank @Size(max = 255) String content
) {}
