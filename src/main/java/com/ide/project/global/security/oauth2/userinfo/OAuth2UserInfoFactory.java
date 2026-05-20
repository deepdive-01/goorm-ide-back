package com.ide.project.global.security.oauth2.userinfo;

import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;

import java.util.Map;

public class OAuth2UserInfoFactory {
    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo getOAuth2UserInfo(
            String registrationId,
            Map<String, Object> attributes
    ) {
        if ("kakao".equals(registrationId)) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        if ("google".equals(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
    }
}
