package com.estratego.infrastructure.persistence.repository;

import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.infrastructure.persistence.entity.EstadoFinancieroEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoFinancieroEmpresaJpaRepository extends JpaRepository<EstadoFinancieroEmpresaEntity, Long> {

    Optional<EstadoFinancieroEmpresaEntity> findByIdEmpresaAndIdCasoAndMomento(
            Long idEmpresa, Long idCaso, MomentoEstado momento);

    List<EstadoFinancieroEmpresaEntity> findByIdCasoOrderByIdEmpresaAscMomentoAsc(Long idCaso);

    List<EstadoFinancieroEmpresaEntity> findByIdEmpresaOrderByCalculadoEnAscIdAsc(Long idEmpresa);

    @Modifying
    @Query("delete from EstadoFinancieroEmpresaEntity e where e.idCaso = :idCaso")
    void eliminarPorCaso(@Param("idCaso") Long idCaso);
}
