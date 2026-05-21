package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.ProblemBank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemBankRepository extends JpaRepository<ProblemBank, Long> {
    // 기본 제공되는 CRUD 메서드(findById 등) 사용
    // 추후 필요 시 List<ProblemBank> findAllByDifficulty(String difficulty); 등을 추가 가능
}