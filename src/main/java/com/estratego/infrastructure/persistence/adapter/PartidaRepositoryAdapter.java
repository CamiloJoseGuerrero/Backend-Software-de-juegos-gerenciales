package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.partida.Partida;
import com.estratego.domain.repository.PartidaRepository;
import com.estratego.infrastructure.persistence.entity.PartidaEntity;
import com.estratego.infrastructure.persistence.repository.PartidaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartidaRepositoryAdapter implements PartidaRepository {

    private final PartidaJpaRepository jpaRepository;

    @Override
    public Optional<Partida> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Partida> findByDocenteId(Long docenteId) {
        return jpaRepository.findByDocenteId(docenteId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Partida save(Partida partida) {
        return toDomain(jpaRepository.save(toEntity(partida)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCasoId(Long casoId) {
        return jpaRepository.existsByCasoId(casoId);
    }

    private Partida toDomain(PartidaEntity e) {
        return new Partida(
                e.getId(),
                e.getCasoId(),
                e.getDocenteId(),
                e.getFechaHoraInicio(),
                e.getFechaHoraCierre(),
                e.getDuracionMinutos(),
                e.getFechaVisualizacion(),
                e.getEstado(),
                e.getEquipoIds()
        );
    }

    private PartidaEntity toEntity(Partida p) {
        return new PartidaEntity(
                p.getId(),
                p.getCasoId(),
                p.getDocenteId(),
                p.getFechaHoraInicio(),
                p.getFechaHoraCierre(),
                p.getDuracionMinutos(),
                p.getFechaVisualizacion(),
                p.getEstado(),
                p.getEquipoIds()
        );
    }
}