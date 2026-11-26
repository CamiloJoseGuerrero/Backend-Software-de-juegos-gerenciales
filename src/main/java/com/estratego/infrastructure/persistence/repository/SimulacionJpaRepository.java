package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.SimulacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionJpaRepository extends JpaRepository<SimulacionEntity, Long> {

    List<SimulacionEntity> findByIdUsuarioCoordinador(Long idUsuarioCoordinador);

    List<SimulacionEntity> findByIdUsuarioCoordinadorAndIdCurso(Long idUsuarioCoordinador, Long idCurso);

    long countByIdCurso(Long idCurso);

    List<SimulacionEntity> findByEstado(com.estratego.domain.model.simulacion.EstadoSimulacion estado);
}
