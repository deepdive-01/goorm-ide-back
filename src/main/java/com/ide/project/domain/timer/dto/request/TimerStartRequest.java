package com.ide.project.domain.timer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TimerStartRequest(

    @NotNull
    Long roomId,

    @NotNull
    @Min(1)
    Integer durationSeconds

) {}
