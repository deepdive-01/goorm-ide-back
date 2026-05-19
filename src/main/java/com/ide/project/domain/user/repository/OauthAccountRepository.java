package com.ide.project.domain.user.repository;

import com.ide.project.domain.user.entity.OauthAccount;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 인터페이스로 구현한 이유는 Spring이 실행 시점에서 자동으로 구현체를 만들어주기 때문
public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {

    // 소셜 로그인시 이미 연동된 계정인지 확인, 카카오로 로그인한 유저가 고유 ID로 기존 계정을 조회할 때
    Optional<OauthAccount> findByProviderAndProviderId(Provider provider, String providerId);

    // 특정 유저가 특정 소셜 로그인을 이미 연동했는지 확인할 때
    Optional<OauthAccount>findByUserAndProvider(User user, Provider provider);
}
