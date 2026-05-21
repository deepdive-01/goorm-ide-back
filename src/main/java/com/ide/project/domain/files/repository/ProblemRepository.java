package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findAllBySpaceId(Long spaceId);
}