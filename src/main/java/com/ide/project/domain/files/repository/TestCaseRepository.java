package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    // 일반 문제의 테스트케이스들을 순서(orderNum)에 맞춰 오름차순 정렬하여 조회
    List<TestCase> findAllByProblemIdOrderByOrderNumAsc(Long problemId);

    // 문제 은행 원본의 테스트케이스들을 순서에 맞춰 오름차순 정렬하여 조회
    List<TestCase> findAllByProblemBankIdOrderByOrderNumAsc(Long problemBankId);
}