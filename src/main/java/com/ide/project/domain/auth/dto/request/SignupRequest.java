package com.ide.project.domain.auth.dto.request;

import com.ide.project.domain.user.entity.Role;
import jakarta.validation.constraints.*;

public record SignupRequest (

    @NotBlank // 비어 있지 않은지
    @Email // 이메일 형식인지
    String email,

    @NotBlank
    // 정규식 검증
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자, 영문+숫자+특수문자를 포함해야 합니다."
    )
    String password,

    @NotBlank
    @Size(max = 50)
    String name,

    @NotBlank
    @Size(max = 50)
    String nickname,

    @NotNull // Null값이 아닌지
    Role role
) {}
