package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.infrastructure.persistence.entity.SimulacionEntity;
import com.estratego.infrastructure.persistence.repository.SimulacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SimulacionRepositoryAdapter implements SimulacionRepository {

    private final SimulacionJpaRepository jpaRepository;

    @Override
    public Optional<Simulacion> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Simulacion> findByIdUsuarioCoordinador(Long idUsuarioCoordinador) {
        return jpaRepository.findByIdUsuarioCoordinador(idUsuarioCoordinador).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Simulacion save(Simulacion simulacion) {
        return toDomain(jpaRepository.save(toEntity(simulacion)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    private Simulacion toDomain(SimulacionEntity e) {
        return new Simulacion(
                e.getId(),
                e.getIdUsuarioCoordinador(),
                e.getNombreCurso(),
                e.getFechaInicio(),
                e.getFechaFin(),
                e.getEstado()
        );
    }

    private SimulacionEntity toEntity(Simulacion s) {
        return new SimulacionEntity(
                s.getId(),
                s.getIdUsuarioCoordinador(),
                s.getNombreCurso(),
                s.getFechaInicio(),
                s.getFechaFin(),
                s.getEstado()
        );
    }
}