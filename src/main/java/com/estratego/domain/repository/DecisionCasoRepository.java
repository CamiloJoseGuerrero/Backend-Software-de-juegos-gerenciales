package com.estratego.domain.repository;

import com.estratego.domain.model.caso.DecisionCaso;

import java.util.List;
import java.util.Optional;

public interface DecisionCasoRepository {

    Optional<DecisionCaso> findByIdCasoAndIdEmpresa(Long idCaso, Long idEmpresa);

    List<DecisionCaso> findByIdCaso(Long idCaso);

    boolean existsByIdCaso(Long idCaso);

    DecisionCaso save(DecisionCaso decision);
}
