package com.ide.project.global.util;

public final class RedisKeys {

    // 인스턴스를 생성하지 못하게
    private RedisKeys() {}

    // 이메일 인증 코드 키 - "email:code:{이메일} 형태로 사용하기 위함"
    public static final String EMAIL_CODE = "email:code:";

    // 이메일 인증 완료 플래그 키 - "email:verified:{이메일}" 형태로 사용
    public static final String EMAIL_VERIFIED = "email:verified:";

    // 리프레시 토큰 키 - "refresh:{userId}" 형태로 사용
    public static final String REFRESH_TOKEN = "refresh:";
}
