package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.EstudianteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteJpaRepository extends JpaRepository<EstudianteEntity, Long> {
}