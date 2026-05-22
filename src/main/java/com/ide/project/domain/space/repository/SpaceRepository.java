package com.ide.project.domain.space.repository;

import com.ide.project.domain.space.entity.Space;
import com.ide.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    // 멘토가 생성한 스페이스 목록
    List<Space> findByMentor(User mentor);

    // 초대코드로 스페이스 찾기
    Optional<Space> findByInviteCodeAndIsActiveTrue(String inviteCode);

}
