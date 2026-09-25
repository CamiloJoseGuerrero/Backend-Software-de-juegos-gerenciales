package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {

    List<EmpresaEntity> findByIdSimulacion(Long idSimulacion);

    Optional<EmpresaEntity> findByIdSimulacionAndCodigoEmpresa(Long idSimulacion, String codigoEmpresa);

    long countByIdSimulacion(Long idSimulacion);
}