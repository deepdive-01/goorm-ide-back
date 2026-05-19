package com.ide.project.global.security.oauth2.userinfo;

public interface OAuth2UserInfo {

    // 제공자 내부 고유 사용자 ID
    String getProviderId();

    // 닉네임
    String getNickname();

    // 이메일
    String getEmail();
}
