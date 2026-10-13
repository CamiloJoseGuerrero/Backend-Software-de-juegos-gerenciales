package com.estratego.domain.repository;

import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;

import java.util.List;
import java.util.Optional;

/** Estados financieros por empresa y caso (arrastre del balance entre casos). */
public interface EstadoFinancieroEmpresaRepository {

    /** Inserta, o reemplaza el que ya exista para la misma empresa, caso y momento. */
    EstadoFinancieroEmpresa guardar(EstadoFinancieroEmpresa estado);

    Optional<EstadoFinancieroEmpresa> buscar(Long idEmpresa, Long idCaso, MomentoEstado momento);

    /** Todos los estados de un caso (todas las empresas, ambos momentos). */
    List<EstadoFinancieroEmpresa> listarPorCaso(Long idCaso);

    /** Historia de una empresa, en orden de cálculo. */
    List<EstadoFinancieroEmpresa> listarPorEmpresa(Long idEmpresa);

    /** Para recalcular un caso (reedición del docente). */
    void eliminarPorCaso(Long idCaso);
}
