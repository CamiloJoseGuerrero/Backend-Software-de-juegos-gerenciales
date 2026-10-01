package com.estratego.infrastructure.persistence.repository;

import com.estratego.infrastructure.persistence.entity.DocenteEstudianteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocenteEstudianteJpaRepository extends JpaRepository<DocenteEstudianteEntity, Long> {

    boolean existsByIdDocenteAndIdEstudiante(Long idDocente, Long idEstudiante);

    @Query("select de.idEstudiante from DocenteEstudianteEntity de " +
           "where de.idDocente = :idDocente order by de.idEstudiante")
    List<Long> findIdsEstudiantes(@Param("idDocente") Long idDocente);
}
