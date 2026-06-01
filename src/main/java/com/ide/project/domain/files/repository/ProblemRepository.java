package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // 특정 스페이스(워크스페이스)에 속한 모든 문제 조회
    List<Problem> findAllBySpaceId(Long spaceId);

    // 특정 강사(생성자)가 등록한 모든 문제 조회
    List<Problem> findAllByCreatedBy(Long createdBy);

    // 특정 스페이스에서 공개된 문제만 조회
    List<Problem> findAllBySpaceIdAndIsPublishedTrue(Long spaceId);
}