package com.estratego.domain.repository;

import com.estratego.domain.model.usuario.Profesor;

import java.util.Optional;

public interface ProfesorRepository {

    Optional<Profesor> findById(Long idProfesor);

    Profesor save(Profesor profesor);

    void deleteById(Long idProfesor);
}