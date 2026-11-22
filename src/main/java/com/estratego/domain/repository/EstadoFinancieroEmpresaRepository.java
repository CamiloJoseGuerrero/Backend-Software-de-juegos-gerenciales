package com.estratego.domain.repository;

import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;

import java.util.List;
import java.util.Optional;

public interface EstadoFinancieroEmpresaRepository {

    EstadoFinancieroEmpresa guardar(EstadoFinancieroEmpresa estado);

    Optional<EstadoFinancieroEmpresa> buscar(Long idEmpresa, Long idCaso, MomentoEstado momento);

    List<EstadoFinancieroEmpresa> listarPorCaso(Long idCaso);

    List<EstadoFinancieroEmpresa> listarPorEmpresa(Long idEmpresa);

    void eliminarPorCaso(Long idCaso);
}
