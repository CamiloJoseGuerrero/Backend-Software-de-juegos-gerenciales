package com.estratego.domain.repository;

import java.util.List;

/** Qué estudiantes pertenecen a cada curso del docente. */
public interface CursoEstudianteRepository {

    boolean existeVinculo(Long idCurso, Long idEstudiante);

    /** Crea el vínculo si no existe. */
    void vincular(Long idCurso, Long idEstudiante);

    void desvincular(Long idCurso, Long idEstudiante);

    List<Long> findIdsEstudiantes(Long idCurso);

    long countByIdCurso(Long idCurso);
}
