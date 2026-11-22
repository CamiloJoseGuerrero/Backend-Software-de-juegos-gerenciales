package com.estratego.domain.repository;

import com.estratego.domain.model.integrante.Integrante;

import java.util.List;
import java.util.Optional;

public interface IntegranteRepository {

    List<Integrante> findByIdEmpresa(Long idEmpresa);

    Optional<Integrante> findByIdEmpresaAndIdUsuario(Long idEmpresa, Long idUsuario);

    long countByIdEmpresa(Long idEmpresa);

    boolean existsEnSimulacion(Long idSimulacion, Long idUsuario);

    void quitarLider(Long idEmpresa);

    Integrante save(Integrante integrante);

    void deleteById(Long id);

    void deleteByIdEmpresa(Long idEmpresa);

    List<Integrante> findByIdUsuario(Long idUsuario);
}
