package com.estratego.infrastructure.persistence.repository;

import com.estratego.domain.model.partida.EstadoPartida;
import com.estratego.infrastructure.persistence.entity.PartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaJpaRepository extends JpaRepository<PartidaEntity, Long> {

    List<PartidaEntity> findByDocenteId(Long docenteId);

    boolean existsByCasoId(Long casoId);

    @Query("SELECT p FROM PartidaEntity p JOIN p.equipoIds e " +
           "WHERE e = :equipoId AND p.estado = :estado AND p.id <> :excluirId")
    List<PartidaEntity> findPartidasConEquipoEnEstado(
            @Param("equipoId") Long equipoId,
            @Param("estado") EstadoPartida estado,
            @Param("excluirId") Long excluirId);
}