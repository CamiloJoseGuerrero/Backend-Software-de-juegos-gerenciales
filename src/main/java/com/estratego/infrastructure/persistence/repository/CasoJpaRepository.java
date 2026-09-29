package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.CasoEntity;
import com.estratego.domain.model.caso.EstadoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CasoJpaRepository extends JpaRepository<CasoEntity, Long> {

    List<CasoEntity> findByIdSimulacionOrderByFechaInicioAsc(Long idSimulacion);

    List<CasoEntity> findByIdSimulacionInOrderByFechaInicioAsc(List<Long> idsSimulacion);

    Optional<CasoEntity> findFirstByIdSimulacionAndEstado(Long idSimulacion, EstadoCaso estado);

    // Se ejecuta de inmediato: así el índice "un activo por simulación" no choca al activar otro
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CasoEntity c set c.estado = com.estratego.domain.model.caso.EstadoCaso.BORRADOR " +
           "where c.idSimulacion = :idSimulacion and c.estado = com.estratego.domain.model.caso.EstadoCaso.ACTIVO")
    int desactivarTodos(@Param("idSimulacion") Long idSimulacion);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CasoEntity c set c.estado = com.estratego.domain.model.caso.EstadoCaso.ACTIVO where c.id = :id")
    int marcarActivo(@Param("id") Long id);
}
