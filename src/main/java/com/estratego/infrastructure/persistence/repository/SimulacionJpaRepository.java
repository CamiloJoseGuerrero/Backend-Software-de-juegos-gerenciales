package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.SimulacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionJpaRepository extends JpaRepository<SimulacionEntity, Long> {

    List<SimulacionEntity> findByIdUsuarioCoordinador(Long idUsuarioCoordinador);
}