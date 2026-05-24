package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.ProblemBank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemBankRepository extends JpaRepository<ProblemBank, Long> {

    // 활성화된 문제 은행 목록만 조회
    List<ProblemBank> findAllByIsActiveTrue();

    // 난이도별 문제 은행 조회
    List<ProblemBank> findAllByDifficulty(String difficulty);

    // 언어별 문제 은행 조회
    List<ProblemBank> findAllByLanguage(String language);
}