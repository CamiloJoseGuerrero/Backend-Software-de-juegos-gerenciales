package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.repository.CursoEstudianteRepository;
import com.estratego.infrastructure.persistence.entity.CursoEstudianteEntity;
import com.estratego.infrastructure.persistence.repository.CursoEstudianteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CursoEstudianteRepositoryAdapter implements CursoEstudianteRepository {

    private final CursoEstudianteJpaRepository jpaRepository;

    @Override
    public boolean existeVinculo(Long idCurso, Long idEstudiante) {
        return jpaRepository.existsByIdCursoAndIdEstudiante(idCurso, idEstudiante);
    }

    @Override
    public void vincular(Long idCurso, Long idEstudiante) {
        if (jpaRepository.existsByIdCursoAndIdEstudiante(idCurso, idEstudiante)) return;
        jpaRepository.save(new CursoEstudianteEntity(null, idCurso, idEstudiante, LocalDateTime.now()));
    }

    @Override
    public void desvincular(Long idCurso, Long idEstudiante) {
        jpaRepository.deleteByIdCursoAndIdEstudiante(idCurso, idEstudiante);
    }

    @Override
    public List<Long> findIdsEstudiantes(Long idCurso) {
        return jpaRepository.findIdsEstudiantes(idCurso);
    }

    @Override
    public long countByIdCurso(Long idCurso) {
        return jpaRepository.countByIdCurso(idCurso);
    }
}
