package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProblemCreateRequest {

    @NotNull(message = "스페이스 ID는 필수입니다.")
    private Long spaceId;

    @NotBlank(message = "문제 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문제 설명은 필수입니다.")
    private String description;

    private String difficulty;

    private String starterCode;

    @NotNull(message = "강사(생성자) ID는 필수입니다.")
    private Long creatorId;
}