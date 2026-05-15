package com.ide.project.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(400, "입력값이 올바르지 않습니다."),

    // 인증
    DUPLICATE_EMAIL(400, "이미 사용 중인 이메일입니다."),
    INVALID_ROLE(400, "유효하지 않은 역할입니다."),
    EMAIL_NOT_VERIFIED(400, "이메일 인증이 완료되지 않았습니다."),
    INVALID_VERIFY_CODE(400, "인증 코드가 올바르지 않습니다."),
    EXPIRED_VERIFY_CODE(400, "인증 코드가 만료됐습니다."),

    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_REQUIRED(401, "로그인이 필요합니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(401, "만료된 토큰입니다."),
    REVOKED_REFRESH_TOKEN(401, "이미 로그아웃된 토큰입니다."),

    INACTIVE_USER(403, "비활성화된 계정입니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    ACCESS_DENIED(403, "접근 권한이 없습니다."),

    USER_NOT_FOUND(404, "존재하지 않는 유저입니다."),

    // 워크스페이스
    SPACE_NOT_FOUND(404, "존재하지 않는 스페이스입니다."),
    INVALID_INVITE_CODE(400, "유효하지 않은 초대 코드입니다."),
    ALREADY_JOINED(400, "이미 참여한 워크스페이스입니다."),

    // 문제
    PROBLEM_NOT_FOUND(404, "존재하지 않는 문항입니다."),
    PROBLEM_BANK_NOT_FOUND(404, "존재하지 않는 문제입니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(404, "알림을 찾을 수 없습니다."),
    INVALID_NOTIFICATION_TYPE(400, "유효하지 않은 알림 타입입니다.");

    private final int status;
    private final String message;

    public String code() {
        return this.name();
    }
}
