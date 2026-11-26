package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.CursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoJpaRepository extends JpaRepository<CursoEntity, Long> {

    List<CursoEntity> findByIdDocenteOrderByFechaCreacionDesc(Long idDocente);
}
