package com.ide.project.domain.space.repository;

import com.ide.project.domain.space.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {
}
