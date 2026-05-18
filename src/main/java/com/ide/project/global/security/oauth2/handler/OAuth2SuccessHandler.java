package com.ide.project.global.security.oauth2.handler;

import com.ide.project.domain.auth.dto.response.TokenResponse;
import com.ide.project.domain.auth.service.AuthService;
import com.ide.project.domain.user.entity.OauthAccount;
import com.ide.project.domain.user.entity.Provider;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.OauthAccountRepository;
import com.ide.project.global.security.oauth2.userinfo.OAuth2UserInfo;
import com.ide.project.global.security.oauth2.userinfo.OAuth2UserInfoFactory;
import com.ide.project.global.util.RedisKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OauthAccountRepository oauthAccountRepository;
    private final AuthService authService;
    private final RedisTemplate<String, String> redisTemplate;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Value("${app.oauth.redirect-url.success}")
    private String successRedirectUrl;

    @Value("${app.oauth.redirect-url.signup}")
    private String signupRedirectUrl;

    // 임시 데이터 보관 시간 (회원가입 시 제공받는 UUID TTL)
    private static final long OAUTH_TEMP_TTL = 10L;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // Spring Security가 완료한 OAuth2 인증 토큰에서 제공자의 정보를 추출
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        // kakao 또는 google의 키 이름
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // 제공자 서버에서 받아온 유저 정보 Map
        OAuth2User oAuth2User = authToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 팩토리를 통해 제공자에게 맞는 파싱을 선택
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        Optional<OauthAccount> oauthAccount = oauthAccountRepository.findByProviderAndProviderId(provider, userInfo.getProviderId());

        // 기존 유저인지, 아닌지를 구분
        if (oauthAccount.isPresent()) {
            // 기존 유저일 경우 AT / RT를 발급하고 FE 메인으로
            handleExistingUser(request, response, oauthAccount.get().getUser());
        } else {
            // 아닐 경우 Redis에 임시 UUID를 저장 후 FE 추가 정보 입력 페이지로 리다이렉트
            handleNewUser(request, response, provider, userInfo);
        }

    }

    // RT는 쿠키, AT는 리다이렉트 URL 쿼리파라미터로 전달하는 핸들러
    private void handleExistingUser(
            HttpServletRequest request,
            HttpServletResponse response,
            User user
    ) throws IOException {
        TokenResponse tokenResponse = authService.oauthLogin(user, response);

        String redirectUrl = UriComponentsBuilder.fromUriString(successRedirectUrl).queryParam("accessToken", tokenResponse.accessToken()).build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void handleNewUser(
            HttpServletRequest request,
            HttpServletResponse response,
            Provider provider,
            OAuth2UserInfo userInfo
    ) throws IOException {

        // UUID로 임시 키를 생성
        String tempKey = UUID.randomUUID().toString();

        // Redis의 저장형식은 "KAKAO::12345678::김구름::"
        // 이메일이 없으면 빈 문자열로 저장
        String email = userInfo.getEmail() != null ? userInfo.getEmail() : "";
        String tempValue = provider.name()
                + "::" + userInfo.getProviderId()
                + "::" + userInfo.getNickname()
                + "::" + email;

        // redis에 저장
        redisTemplate.opsForValue().set(
                RedisKeys.OAUTH_TEMP + tempKey,
                tempValue,
                OAUTH_TEMP_TTL,
                TimeUnit.MINUTES
        );

        // redirectUrl 생성
        String redirectUrl = UriComponentsBuilder.fromUriString(signupRedirectUrl).queryParam("tempKey", tempKey).build().toUriString();

        // 전송
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

}
