package com.estratego.domain.repository;

import com.estratego.domain.model.curso.Curso;

import java.util.List;
import java.util.Optional;

public interface CursoRepository {

    Optional<Curso> findById(Long id);

    List<Curso> findByIdDocente(Long idDocente);

    Curso save(Curso curso);

    void deleteById(Long id);
}
