package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.ProfesorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesorJpaRepository extends JpaRepository<ProfesorEntity, Long> {
}