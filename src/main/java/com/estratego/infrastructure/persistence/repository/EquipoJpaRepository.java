package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.EquipoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoJpaRepository extends JpaRepository<EquipoEntity, Long> {

    List<EquipoEntity> findByDocenteId(Long docenteId);

    long countByDocenteId(Long docenteId);

    List<EquipoEntity> findByIdIn(List<Long> ids);
}