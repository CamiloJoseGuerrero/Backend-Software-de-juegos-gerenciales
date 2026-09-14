package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.equipo.Equipo;
import com.estratego.domain.repository.EquipoRepository;
import com.estratego.infrastructure.persistence.entity.EquipoEntity;
import com.estratego.infrastructure.persistence.repository.EquipoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EquipoRepositoryAdapter implements EquipoRepository {

    private final EquipoJpaRepository jpaRepository;

    @Override
    public Optional<Equipo> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Equipo> findByDocenteId(Long docenteId) {
        return jpaRepository.findByDocenteId(docenteId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Equipo save(Equipo equipo) {
        EquipoEntity saved = jpaRepository.save(toEntity(equipo));
        return toDomain(saved);
    }

    @Override
    public long countByDocenteId(Long docenteId) {
        return jpaRepository.countByDocenteId(docenteId);
    }

    private Equipo toDomain(EquipoEntity entity) {
        return new Equipo(
                entity.getId(),
                entity.getNombre(),
                entity.getDocenteId(),
                entity.getLiderId(),
                entity.getEstudianteIds()
        );
    }

    private EquipoEntity toEntity(Equipo equipo) {
        return new EquipoEntity(
                equipo.getId(),
                equipo.getNombre(),
                equipo.getDocenteId(),
                equipo.getLiderId(),
                equipo.getEstudianteIds()
        );
    }
}