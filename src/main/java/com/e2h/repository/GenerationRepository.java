package com.e2h.repository;

import com.e2h.entity.GenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerationRepository extends JpaRepository<GenerationEntity, String> {
}
