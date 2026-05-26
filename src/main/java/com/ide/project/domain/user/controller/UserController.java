package com.ide.project.domain.user.controller;

import com.ide.project.domain.user.dto.response.ProfileImageResponse;
import com.ide.project.domain.user.dto.response.UserMeResponse;
import com.ide.project.domain.user.service.UserService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @Operation(
            summary = "내 정보 조회",
            description = "본인 정보를 조회할 수 있는 엔드 포인트입니다. AccessToken 인증이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        UserMeResponse response = userService.getMe(userId);

        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "내 정보를 조회했습니다.", response));
    }

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "multipart/form-data로 이미지를 전송합니다. AccessToken 인증이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
            @RequestPart("image")MultipartFile file
            ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        ProfileImageResponse response = userService.uploadProfileImage(userId, file);

        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "프로필 이미지가 업로드되었습니다.", response));
    }

    @Operation(
            summary = "프로필 이미지 삭제",
            description = "프로필 이미지를 삭제합니다. AccessToken 인증이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        userService.deleteProfileImage(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "프로필 이미지가 삭제되었습니다."));
    }

}
