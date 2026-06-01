package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findAllByProblemId(Long problemId);

    List<TestCase> findAllByProblemBankId(Long problemBankId);

    void deleteAllByProblemId(Long problemId);

    void deleteAllByProblemBankId(Long problemBankId);
}