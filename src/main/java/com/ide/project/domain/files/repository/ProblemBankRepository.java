package com.ide.project.domain.files.repository;

import com.ide.project.domain.files.entity.ProblemBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemBankRepository extends JpaRepository<ProblemBank, Long> {
    // 기본 CRUD(save, findById 등) 자동 제공
}