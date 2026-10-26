package com.estratego.domain.repository;

import com.estratego.domain.model.boletin.BoletinCaso;

import java.util.Optional;

public interface BoletinCasoRepository {

    Optional<BoletinCaso> findByIdCaso(Long idCaso);

    BoletinCaso save(BoletinCaso boletin);

    void deleteByIdCaso(Long idCaso);

    boolean existsByIdCaso(Long idCaso);
}
