package com.ide.project.domain.space.repository;

import com.ide.project.domain.space.entity.Space;
import com.ide.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    // 멘토가 생성한 스페이스 목록
    List<Space> findByMentor(User mentor);

    // 초대 코드로 조회
    Optional<Space> findByInviteCode(String inviteCode);

    // 초대 코드 중복 여부 확인
    boolean existsByInviteCode(String inviteCode);

}
