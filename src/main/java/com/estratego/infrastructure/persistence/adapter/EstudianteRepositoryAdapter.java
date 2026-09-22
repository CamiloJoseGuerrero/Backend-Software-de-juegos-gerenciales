package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.infrastructure.persistence.entity.EstudianteEntity;
import com.estratego.infrastructure.persistence.repository.EstudianteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstudianteRepositoryAdapter implements EstudianteRepository {

    private final EstudianteJpaRepository jpaRepository;

    @Override
    public Optional<Estudiante> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Estudiante save(Estudiante estudiante) {
        EstudianteEntity saved = jpaRepository.save(toEntity(estudiante));
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Estudiante toDomain(EstudianteEntity e) {
        return new Estudiante(
                e.getIdEstudiante(),
                e.getCodigoEstudiantil(),
                e.getCarrera(),
                e.getSemestre(),
                e.getEdad(),
                e.getGenero()
        );
    }

    private EstudianteEntity toEntity(Estudiante e) {
        return new EstudianteEntity(
                e.getIdEstudiante(),
                e.getCodigoEstudiantil(),
                e.getCarrera(),
                e.getSemestre(),
                e.getEdad(),
                e.getGenero()
        );
    }
}