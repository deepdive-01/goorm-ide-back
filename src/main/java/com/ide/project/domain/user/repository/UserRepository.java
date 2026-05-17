package com.ide.project.domain.user.repository;

import com.ide.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 유저 찾기
    Optional<User> findByEmail(String email);

    // 중복된 이메일이 있는지 확인
    boolean existsByEmail(String email);
}
