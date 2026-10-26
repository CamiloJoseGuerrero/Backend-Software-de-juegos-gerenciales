package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.BoletinCasoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoletinCasoJpaRepository extends JpaRepository<BoletinCasoEntity, Long> {

    Optional<BoletinCasoEntity> findByIdCaso(Long idCaso);

    void deleteByIdCaso(Long idCaso);

    boolean existsByIdCaso(Long idCaso);
}
