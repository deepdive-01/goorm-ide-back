package com.ide.project.domain.auth.controller;

import com.ide.project.domain.auth.dto.request.*;
import com.ide.project.domain.auth.dto.response.SignupResponse;
import com.ide.project.domain.auth.dto.response.TokenResponse;
import com.ide.project.domain.auth.service.AuthService;
import com.ide.project.domain.auth.service.EmailVerifyService;
import com.ide.project.domain.auth.service.OAuthSignupService;
import com.ide.project.domain.auth.service.SignupService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SignupService signupService;
    private final EmailVerifyService emailVerifyService;
    private final OAuthSignupService oAuthSignupService;

    // POST /api/v1/auth/email/send  이메일 인증코드 발송
    @Operation(summary = "이메일 인증코드 발송", description = "회원가입 전 이메일 인증코드를 발송합니다. 코드 유효시간은 5분입니다.")
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid
            @RequestBody
            EmailSendRequest request
    ) {
        emailVerifyService.sendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "인증 코드가 발송되었습니다."));
    }

    // POST /api/v1/auth/email/verify 이메일 인증코드 확인
    @Operation(summary = "이메일 인증코드 확인", description = "발송된 6자리 인증코드를 검증합니다. 인증 완료 상태는 30분간 유지됩니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid
            @RequestBody
            EmailVerifyRequest request
    ) {
        emailVerifyService.verifyCode(request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "이메일 인증이 완료되었습니다."));
    }

    // POST /api/v1/auth/signup 회원가입
    @Operation(summary = "로컬 회원가입", description = "이메일 인증을 완료한 사용자가 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid
            @RequestBody
            SignupRequest request // 회원가입 request DTO
    ) {
        // 회원가입 응답 DTO를 통해 회원가입 로직을 거친 값을 넣음
        SignupResponse signupResponse = signupService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "회원가입이 완료되었습니다", signupResponse));
    }

    // POST /api/v1/auth/login 로컬 로그인
    @Operation(summary = "로컬 로그인", description = "이메일/비밀번호로 로그인합니다. AccessToken은 응답 body, RefreshToken은 HttpOnly 쿠키로 전달됩니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid
            @RequestBody
            LoginRequest request, // 로그인 요청 DTO
            HttpServletResponse response
    ) {
        // 토큰 발급 DTO에 로그인 완료 정보를 담아서 제공
        TokenResponse tokenResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "로그인 성공", tokenResponse));
    }

    // POST /api/v1/auth/logout 로그아웃 (AT 필요)
    // JwtAuthenticationFilter가 principal에 userId를 저장해두었기 때문에 <- 잘 모르겠어요 이건
    @Operation(summary = "로그아웃", description = "AccessToken 인증이 필요합니다. Redis의 RefreshToken을 삭제하고 쿠키를 만료시킵니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletResponse response
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        authService.logout(userId, response);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "로그아웃 되었습니다."));
    }

    // POST /api/v1/auth/refresh AT 재발급
    // RT를 통해 재발급을 진행, @CookieValue를 이용하면 RT를 추출할 수있음
    @Operation(summary = "AccessToken 재발급", description = "쿠키의 RefreshToken을 검증하여 새로운 AccessToken을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @CookieValue(name = "refreshToken") // refreshToken이라는 이름을 찾아서
            String refreshToken // 저장
    ) {
        TokenResponse tokenResponse = authService.reissue(refreshToken);;
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "토큰이 재발급 되었습니다.", tokenResponse));
    }

    // POST /api/v1/auth/oauth/signup 소셜 신규 유저 추가 정보 입력
    // tempKey로 Redis에서 providerId, nickname을 꺼내, User + OauthAccount를 생성합니다.
    @Operation(summary = "소셜 신규 유저 회원가입", description = "소셜 로그인 후 신규 유저가 추가 정보를 입력하여 회원가입을 완료합니다. tempKey는 소셜 로그인 리다이렉트 시 쿼리파라미터로 전달됩니다.")
    @PostMapping("/oauth/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> oauthSignup(
            @Valid
            @RequestBody
            OAuthSignupRequest request,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = oAuthSignupService.signup(request, response);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "회원가입이 완료되었습니다.", tokenResponse));
    }
}
