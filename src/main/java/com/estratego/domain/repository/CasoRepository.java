package com.estratego.domain.repository;

import com.estratego.domain.model.caso.Caso;

import java.util.List;
import java.util.Optional;

public interface CasoRepository {

    Optional<Caso> findById(Long id);

    List<Caso> findByIdSimulacion(Long idSimulacion);

    Caso save(Caso caso);

    List<Caso> findByIdSimulacionIn(List<Long> idsSimulacion);

    Optional<Caso> findActivoBySimulacion(Long idSimulacion);

    void desactivarTodos(Long idSimulacion);

    void marcarActivo(Long id);

    void deleteById(Long id);
}
