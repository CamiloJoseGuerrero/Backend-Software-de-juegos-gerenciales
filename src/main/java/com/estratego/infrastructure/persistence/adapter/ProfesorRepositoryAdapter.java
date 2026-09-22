package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.usuario.Profesor;
import com.estratego.domain.repository.ProfesorRepository;
import com.estratego.infrastructure.persistence.entity.ProfesorEntity;
import com.estratego.infrastructure.persistence.repository.ProfesorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfesorRepositoryAdapter implements ProfesorRepository {

    private final ProfesorJpaRepository jpaRepository;

    @Override
    public Optional<Profesor> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Profesor save(Profesor profesor) {
        ProfesorEntity saved = jpaRepository.save(toEntity(profesor));
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Profesor toDomain(ProfesorEntity e) {
        return new Profesor(e.getIdProfesor(), e.getDepartamento(), e.getTituloAcademico());
    }

    private ProfesorEntity toEntity(Profesor p) {
        return new ProfesorEntity(p.getIdProfesor(), p.getDepartamento(), p.getTituloAcademico());
    }
}