package com.ide.project.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendRequest (
        @NotBlank
        @Email
        String email
) {}
