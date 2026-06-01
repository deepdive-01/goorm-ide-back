package com.ide.project.domain.workspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnterRequest(

        @NotNull
        Long userId,

        @NotBlank
        String nickname

) {}
