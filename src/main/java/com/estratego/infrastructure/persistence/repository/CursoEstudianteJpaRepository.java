package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.CursoEstudianteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CursoEstudianteJpaRepository extends JpaRepository<CursoEstudianteEntity, Long> {

    boolean existsByIdCursoAndIdEstudiante(Long idCurso, Long idEstudiante);

    void deleteByIdCursoAndIdEstudiante(Long idCurso, Long idEstudiante);

    long countByIdCurso(Long idCurso);

    @Query("SELECT ce.idEstudiante FROM CursoEstudianteEntity ce WHERE ce.idCurso = :idCurso ORDER BY ce.fechaVinculo ASC")
    List<Long> findIdsEstudiantes(@Param("idCurso") Long idCurso);
}
