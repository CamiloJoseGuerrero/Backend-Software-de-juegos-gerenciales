package com.estratego.domain.repository;

import com.estratego.domain.model.usuario.Estudiante;

import java.util.Optional;

public interface EstudianteRepository {

    Optional<Estudiante> findById(Long idEstudiante);

    Estudiante save(Estudiante estudiante);

    void deleteById(Long idEstudiante);
}