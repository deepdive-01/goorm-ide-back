package com.ide.project.domain.space.repository;

import com.ide.project.domain.space.entity.SpaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceMemberRepository extends JpaRepository<SpaceMember, Long> {

    // 학생이 참여한 스페이스 목록
    List<SpaceMember> findByUserId(Long userId);

    // 중복 참여 확인
    boolean existsBySpaceIdAndUserId(Long spaceId, Long userId);

    // 스페이스 내 멤버 목록
    List<SpaceMember> findBySpaceId(Long spaceId);

}
