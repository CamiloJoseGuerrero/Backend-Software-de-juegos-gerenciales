package com.estratego.domain.repository;

import com.estratego.domain.model.simulacion.Simulacion;

import java.util.List;
import java.util.Optional;

public interface SimulacionRepository {

    Optional<Simulacion> findById(Long id);

    List<Simulacion> findByIdUsuarioCoordinador(Long idUsuarioCoordinador);

    Simulacion save(Simulacion simulacion);

    void deleteById(Long id);

    boolean existsById(Long id);
}