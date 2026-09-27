package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.infrastructure.persistence.entity.IntegranteEntity;
import com.estratego.infrastructure.persistence.repository.IntegranteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IntegranteRepositoryAdapter implements IntegranteRepository {

    private final IntegranteJpaRepository jpaRepository;

    @Override
    public List<Integrante> findByIdEmpresa(Long idEmpresa) {
        return jpaRepository.findByIdEmpresa(idEmpresa).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Integrante> findByIdEmpresaAndIdUsuario(Long idEmpresa, Long idUsuario) {
        return jpaRepository.findByIdEmpresaAndIdUsuario(idEmpresa, idUsuario).map(this::toDomain);
    }

    @Override
    public long countByIdEmpresa(Long idEmpresa) {
        return jpaRepository.countByIdEmpresa(idEmpresa);
    }

    @Override
    public boolean existsEnSimulacion(Long idSimulacion, Long idUsuario) {
        return jpaRepository.existsEnSimulacion(idSimulacion, idUsuario);
    }

    @Override
    public void quitarLider(Long idEmpresa) {
        jpaRepository.quitarLider(idEmpresa);
    }

    @Override
    public Integrante save(Integrante integrante) {
        return toDomain(jpaRepository.save(toEntity(integrante)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByIdEmpresa(Long idEmpresa) {
        jpaRepository.deleteByIdEmpresa(idEmpresa);
    }

    private Integrante toDomain(IntegranteEntity e) {
        return new Integrante(e.getId(), e.getIdEmpresa(), e.getIdUsuario(),
                e.getDepartamento(), e.isEsLider());
    }

    private IntegranteEntity toEntity(Integrante i) {
        return new IntegranteEntity(i.getId(), i.getIdEmpresa(), i.getIdUsuario(),
                i.getDepartamento(), i.isEsLider());
    }
}
