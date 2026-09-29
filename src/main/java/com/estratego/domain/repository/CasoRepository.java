package com.estratego.domain.repository;

import com.estratego.domain.model.caso.Caso;

import java.util.List;
import java.util.Optional;

public interface CasoRepository {

    Optional<Caso> findById(Long id);

    List<Caso> findByIdSimulacion(Long idSimulacion);

    /** Guarda el caso y reemplaza todas sus opciones por las del objeto. */
    Caso save(Caso caso);

    List<Caso> findByIdSimulacionIn(List<Long> idsSimulacion);

    Optional<Caso> findActivoBySimulacion(Long idSimulacion);

    /** Pasa a BORRADOR el caso ACTIVO de la simulación, si lo hay (se ejecuta de inmediato). */
    void desactivarTodos(Long idSimulacion);

    /** Pone el caso en ACTIVO sin tocar sus opciones (se ejecuta de inmediato). */
    void marcarActivo(Long id);

    void deleteById(Long id);
}
