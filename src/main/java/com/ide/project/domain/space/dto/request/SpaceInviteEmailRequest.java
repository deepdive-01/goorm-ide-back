package com.ide.project.domain.space.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SpaceInviteEmailRequest (
   @NotEmpty
   @Size(max = 10)
   List<@Email String> emails
) {}
