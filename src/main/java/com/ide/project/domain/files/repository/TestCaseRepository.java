package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    // 일반 문제의 테스트케이스 순서대로 조회
    List<TestCase> findAllByProblemIdOrderByOrderNumAsc(Long problemId);

    // 문제 은행 원본의 테스트케이스 순서대로 조회
    List<TestCase> findAllByProblemBankIdOrderByOrderNumAsc(Long problemBankId);

    // 특정 문제의 테스트케이스 전체 삭제 (saveTestCases 중복 누적 방지)
    void deleteAllByProblemId(Long problemId);
}