package com.estratego.domain.repository;

import com.estratego.domain.model.partida.Partida;

import java.util.List;
import java.util.Optional;

public interface PartidaRepository {

    Optional<Partida> findById(Long id);

    List<Partida> findByDocenteId(Long docenteId);

    Partida save(Partida partida);

    void deleteById(Long id);

    boolean existsByCasoId(Long casoId);

    boolean existeEquipoEnPartidaEnCurso(Long equipoId, Long excluirPartidaId);
}