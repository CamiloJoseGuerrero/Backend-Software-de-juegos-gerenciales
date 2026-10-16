package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.repository.EstadoFinancieroEmpresaRepository;
import com.estratego.infrastructure.persistence.entity.EstadoFinancieroEmpresaEntity;
import com.estratego.infrastructure.persistence.repository.EstadoFinancieroEmpresaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstadoFinancieroEmpresaRepositoryAdapter implements EstadoFinancieroEmpresaRepository {

    private final EstadoFinancieroEmpresaJpaRepository jpaRepository;

    @Override
    @Transactional
    public EstadoFinancieroEmpresa guardar(EstadoFinancieroEmpresa e) {
        // Upsert por (empresa, caso, momento): conserva el id de la fila existente
        Long id = jpaRepository
                .findByIdEmpresaAndIdCasoAndMomento(e.getIdEmpresa(), e.getIdCaso(), e.getMomento())
                .map(EstadoFinancieroEmpresaEntity::getId)
                .orElse(null);
        EstadoFinancieroEmpresaEntity entity = new EstadoFinancieroEmpresaEntity(
                id, e.getIdEmpresa(), e.getIdCaso(), e.getMomento(), e.getOrigen(), e.getIdOpcion(),
                e.getCalculadoEn() != null ? e.getCalculadoEn() : LocalDateTime.now(),
                e.getPenalizacionPorcentaje(),
                CasoRepositoryAdapter.aEmbeddable(e.getEstado()));
        return aDominio(jpaRepository.save(entity));
    }

    @Override
    public Optional<EstadoFinancieroEmpresa> buscar(Long idEmpresa, Long idCaso, MomentoEstado momento) {
        return jpaRepository.findByIdEmpresaAndIdCasoAndMomento(idEmpresa, idCaso, momento)
                .map(EstadoFinancieroEmpresaRepositoryAdapter::aDominio);
    }

    @Override
    public List<EstadoFinancieroEmpresa> listarPorCaso(Long idCaso) {
        return jpaRepository.findByIdCasoOrderByIdEmpresaAscMomentoAsc(idCaso).stream()
                .map(EstadoFinancieroEmpresaRepositoryAdapter::aDominio).toList();
    }

    @Override
    public List<EstadoFinancieroEmpresa> listarPorEmpresa(Long idEmpresa) {
        return jpaRepository.findByIdEmpresaOrderByCalculadoEnAscIdAsc(idEmpresa).stream()
                .map(EstadoFinancieroEmpresaRepositoryAdapter::aDominio).toList();
    }

    @Override
    @Transactional
    public void eliminarPorCaso(Long idCaso) {
        jpaRepository.eliminarPorCaso(idCaso);
    }

    private static EstadoFinancieroEmpresa aDominio(EstadoFinancieroEmpresaEntity e) {
        return new EstadoFinancieroEmpresa(
                e.getId(), e.getIdEmpresa(), e.getIdCaso(), e.getMomento(), e.getOrigen(), e.getIdOpcion(),
                CasoRepositoryAdapter.aDominio(e.getEstado()), e.getCalculadoEn(), e.getPenalizacionPorcentaje());
    }
}
