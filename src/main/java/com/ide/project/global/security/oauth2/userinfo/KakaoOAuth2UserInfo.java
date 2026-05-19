package com.ide.project.global.security.oauth2.userinfo;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 카카오 고유 ID
    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    // kakao_account -> profile -> nickname 순서로 꺼냄
    @Override
    public String getNickname() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return null;

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) return null;

        return (String) profile.get("nickname");
    }

    // 비즈앱이 아니기 때문에 null로 채워짐
    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return null;

        return (String) kakaoAccount.get("email");
    }

}
