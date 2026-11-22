package com.estratego.domain.repository;

import java.util.List;

public interface DocenteEstudianteRepository {

    boolean existeVinculo(Long idDocente, Long idEstudiante);

    void vincular(Long idDocente, Long idEstudiante);

    List<Long> findIdsEstudiantes(Long idDocente);
}
