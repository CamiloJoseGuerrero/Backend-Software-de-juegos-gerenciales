package com.estratego.domain.repository;

import java.util.List;

/** Qué estudiantes cargó cada docente (manual o por Excel). */
public interface DocenteEstudianteRepository {

    boolean existeVinculo(Long idDocente, Long idEstudiante);

    /** Crea el vínculo si no existe. */
    void vincular(Long idDocente, Long idEstudiante);

    List<Long> findIdsEstudiantes(Long idDocente);
}
