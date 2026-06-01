package com.ide.project.global.security.filter;

import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.security.jwt.JwtProvider;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 모든 HTTP 요청이 컨트롤러에 도달하기 전에 이 필터를 거쳐서 검증을 진행
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        // 요청 헤더에서 토큰을 추출
        String token = resolveToken(request);

        // 토큰이 없을 경우를 제외하고 토큰이 있는 경우 모두 검증
        if (token != null) {
            try {
                jwtProvider.validateToken(token);

                Long userId = jwtProvider.getUserId(token);
                String role = jwtProvider.getRole(token);

                UsernamePasswordAuthenticationToken authenticationToken =

                        // 인증된 사용자인지를 검증
                        // 파라미터 1: 인증 주체를 확인 (userId)
                        // 파라미터 2: 비밀번호인데 JWT 방식에선 불필요
                        // 파라미터 3: 권한 목록
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // 인증 정보를 저장하고
                // 이후 컨트롤러에서 @AuthenticationPrincipal 등으로 꺼내 사용할 수 있음
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (BusinessException e) {
                // 유효하지 않은 토큰일 경우 비어둠
                SecurityContextHolder.clearContext();
            }
        }
        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }

    // 헤더에서 토큰을 추출
    // 헤더 방식: "Bearer eyALKDGJADGLdalgkjdg..."
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        // "Bearer " 문자열을 잘라서 반환
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }


}
