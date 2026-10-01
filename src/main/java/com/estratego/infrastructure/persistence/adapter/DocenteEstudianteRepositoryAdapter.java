package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.infrastructure.persistence.entity.DocenteEstudianteEntity;
import com.estratego.infrastructure.persistence.repository.DocenteEstudianteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocenteEstudianteRepositoryAdapter implements DocenteEstudianteRepository {

    private final DocenteEstudianteJpaRepository jpaRepository;

    @Override
    public boolean existeVinculo(Long idDocente, Long idEstudiante) {
        return jpaRepository.existsByIdDocenteAndIdEstudiante(idDocente, idEstudiante);
    }

    @Override
    public void vincular(Long idDocente, Long idEstudiante) {
        if (jpaRepository.existsByIdDocenteAndIdEstudiante(idDocente, idEstudiante)) return;
        jpaRepository.save(new DocenteEstudianteEntity(null, idDocente, idEstudiante, LocalDateTime.now()));
    }

    @Override
    public List<Long> findIdsEstudiantes(Long idDocente) {
        return jpaRepository.findIdsEstudiantes(idDocente);
    }
}
