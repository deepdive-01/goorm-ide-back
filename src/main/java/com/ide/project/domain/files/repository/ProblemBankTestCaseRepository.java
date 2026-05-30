package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.ProblemBankTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemBankTestCaseRepository extends JpaRepository<ProblemBankTestCase, Long> {

    // 문제 은행 원본의 테스트케이스 순서대로 조회
    List<ProblemBankTestCase> findAllByProblemBankIdOrderByOrderNumAsc(Long problemBankId);
}