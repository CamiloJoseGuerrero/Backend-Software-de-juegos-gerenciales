package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.curso.Curso;
import com.estratego.domain.repository.CursoRepository;
import com.estratego.infrastructure.persistence.entity.CursoEntity;
import com.estratego.infrastructure.persistence.repository.CursoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CursoRepositoryAdapter implements CursoRepository {

    private final CursoJpaRepository jpaRepository;

    @Override
    public Optional<Curso> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Curso> findByIdDocente(Long idDocente) {
        return jpaRepository.findByIdDocenteOrderByFechaCreacionDesc(idDocente).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Curso save(Curso curso) {
        return toDomain(jpaRepository.save(toEntity(curso)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Curso toDomain(CursoEntity e) {
        return new Curso(e.getId(), e.getIdDocente(), e.getNombre(), e.getFechaCreacion());
    }

    private CursoEntity toEntity(Curso c) {
        return new CursoEntity(c.getId(), c.getIdDocente(), c.getNombre(), c.getFechaCreacion());
    }
}
