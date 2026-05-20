package com.ide.project.global.security.jwt;

import com.ide.project.domain.user.entity.Role;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Spring Bean으로 등록 - 다른 클래스에서 주입받아서 사용할 수 있게
@Component
public class JwtProvider {

    private final SecretKey secretKey;

    private final long accessExpiration; // AT 민료 시간
    private final long refreshExpiration; // RT 만료 시간
    private DelegatingFilterProxyRegistrationBean securityFilterChainRegistration;

    // 생성자
    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        // 비밀키를 SHA 알로리즘용 SecretKey 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // AccessToken 생성
    public String generateAccessToken(Long userId, Role role) {
        return Jwts.builder()
                .subject(String.valueOf(userId)) // 토큰 주인의 주인 식별
                .claim("role", role.name()) // 토큰 안에 추가로 담을 정보
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    // RefreshToken 생성
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId)) // 오직 AT 발급 목적이므로 유저 ID만 저장
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration)) // 만료시간이 AT에 비해 더 김
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject()); // 반환된 토큰 값에서 Subject를 꺼냄(유저 Id)
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class); // 토큰에서 role에 해당하는 값을 추출

    }

    // 토큰 유효성 검증
    public void validateToken(String token) {
        try {
            getClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN); // 만료된 토큰일 경우
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN); // 유효하지 않은 토큰일 경우
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // 서명 검증에 사용할 키를 지정하고
                .build()
                .parseSignedClaims(token) // 토큰을 파싱하면서 검증을 진행
                .getPayload(); // Claims 데이터를 반환

    }

}
