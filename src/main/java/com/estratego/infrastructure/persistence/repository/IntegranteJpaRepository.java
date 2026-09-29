package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.IntegranteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegranteJpaRepository extends JpaRepository<IntegranteEntity, Long> {

    List<IntegranteEntity> findByIdEmpresa(Long idEmpresa);

    Optional<IntegranteEntity> findByIdEmpresaAndIdUsuario(Long idEmpresa, Long idUsuario);

    long countByIdEmpresa(Long idEmpresa);

    @Query("select count(i) > 0 from IntegranteEntity i, EmpresaEntity e " +
           "where e.id = i.idEmpresa and e.idSimulacion = :idSimulacion and i.idUsuario = :idUsuario")
    boolean existsEnSimulacion(@Param("idSimulacion") Long idSimulacion,
                               @Param("idUsuario") Long idUsuario);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update IntegranteEntity i set i.esLider = false " +
           "where i.idEmpresa = :idEmpresa and i.esLider = true")
    int quitarLider(@Param("idEmpresa") Long idEmpresa);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from IntegranteEntity i where i.idEmpresa = :idEmpresa")
    int deleteByIdEmpresa(@Param("idEmpresa") Long idEmpresa);

    List<IntegranteEntity> findByIdUsuario(Long idUsuario);
}
