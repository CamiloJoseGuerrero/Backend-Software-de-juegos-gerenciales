package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.PartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaJpaRepository extends JpaRepository<PartidaEntity, Long> {

    List<PartidaEntity> findByDocenteId(Long docenteId);

    boolean existsByCasoId(Long casoId);
}