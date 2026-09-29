package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.DecisionCasoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DecisionCasoJpaRepository extends JpaRepository<DecisionCasoEntity, Long> {

    Optional<DecisionCasoEntity> findByIdCasoAndIdEmpresa(Long idCaso, Long idEmpresa);

    List<DecisionCasoEntity> findByIdCaso(Long idCaso);

    boolean existsByIdCaso(Long idCaso);
}
