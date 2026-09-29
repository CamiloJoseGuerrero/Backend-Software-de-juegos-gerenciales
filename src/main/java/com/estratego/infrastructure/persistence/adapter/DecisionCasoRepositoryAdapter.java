package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.repository.DecisionCasoRepository;
import com.estratego.infrastructure.persistence.entity.DecisionCasoEntity;
import com.estratego.infrastructure.persistence.repository.DecisionCasoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DecisionCasoRepositoryAdapter implements DecisionCasoRepository {

    private final DecisionCasoJpaRepository jpa;

    @Override
    public Optional<DecisionCaso> findByIdCasoAndIdEmpresa(Long idCaso, Long idEmpresa) {
        return jpa.findByIdCasoAndIdEmpresa(idCaso, idEmpresa).map(this::toDomain);
    }

    @Override
    public List<DecisionCaso> findByIdCaso(Long idCaso) {
        return jpa.findByIdCaso(idCaso).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByIdCaso(Long idCaso) {
        return jpa.existsByIdCaso(idCaso);
    }

    @Override
    public DecisionCaso save(DecisionCaso d) {
        return toDomain(jpa.save(new DecisionCasoEntity(
                d.getId(), d.getIdCaso(), d.getIdEmpresa(), d.getIdOpcion(), d.getIdUsuario(), d.getFechaDecision())));
    }

    private DecisionCaso toDomain(DecisionCasoEntity e) {
        return new DecisionCaso(e.getId(), e.getIdCaso(), e.getIdEmpresa(), e.getIdOpcion(),
                e.getIdUsuario(), e.getFechaDecision());
    }
}
