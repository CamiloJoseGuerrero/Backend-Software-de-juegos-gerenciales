package com.estratego.domain.repository;

import com.estratego.domain.model.equipo.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository {

    Optional<Equipo> findById(Long id);

    List<Equipo> findByDocenteId(Long docenteId);

    List<Equipo> findByIdIn(List<Long> ids);

    Equipo save(Equipo equipo);

    long countByDocenteId(Long docenteId);
}