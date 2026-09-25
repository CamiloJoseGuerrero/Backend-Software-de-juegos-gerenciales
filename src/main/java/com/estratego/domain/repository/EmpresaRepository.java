package com.estratego.domain.repository;

import com.estratego.domain.model.empresa.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {

    Optional<Empresa> findById(Long id);

    List<Empresa> findByIdSimulacion(Long idSimulacion);

    Optional<Empresa> findByIdSimulacionAndCodigoEmpresa(Long idSimulacion, String codigoEmpresa);

    long countByIdSimulacion(Long idSimulacion);

    Empresa save(Empresa empresa);

    void deleteById(Long id);
}