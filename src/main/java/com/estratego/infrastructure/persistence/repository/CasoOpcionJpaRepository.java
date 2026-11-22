package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.CasoOpcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasoOpcionJpaRepository extends JpaRepository<CasoOpcionEntity, Long> {

    List<CasoOpcionEntity> findByIdCasoOrderByOrdenAsc(Long idCaso);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CasoOpcionEntity o where o.idCaso = :idCaso")
    int deleteByIdCaso(@Param("idCaso") Long idCaso);
}
