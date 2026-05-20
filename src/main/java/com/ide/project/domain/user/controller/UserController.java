package com.ide.project.domain.user.controller;

import com.ide.project.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 탈퇴
    // JWT 필터를 통과한 인증된 유저만 접근 가능
    // SecurityContext에서 꺼낸 userId로 탈퇴 처리
    @Operation(summary = "회원 탈퇴", description = "AccessToken 인증이 필요합니다. 계정을 비활성화(소프트 삭제)하고 자동 로그아웃 처리합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        userService.withdraw(userId, response);

        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
