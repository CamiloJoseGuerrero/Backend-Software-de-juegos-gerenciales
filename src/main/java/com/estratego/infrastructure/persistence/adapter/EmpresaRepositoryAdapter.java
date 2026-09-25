package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.infrastructure.persistence.entity.EmpresaEntity;
import com.estratego.infrastructure.persistence.repository.EmpresaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpresaRepositoryAdapter implements EmpresaRepository {

    private final EmpresaJpaRepository jpaRepository;

    @Override
    public Optional<Empresa> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Empresa> findByIdSimulacion(Long idSimulacion) {
        return jpaRepository.findByIdSimulacion(idSimulacion).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Empresa> findByIdSimulacionAndCodigoEmpresa(Long idSimulacion, String codigoEmpresa) {
        return jpaRepository.findByIdSimulacionAndCodigoEmpresa(idSimulacion, codigoEmpresa)
                .map(this::toDomain);
    }

    @Override
    public long countByIdSimulacion(Long idSimulacion) {
        return jpaRepository.countByIdSimulacion(idSimulacion);
    }

    @Override
    public Empresa save(Empresa empresa) {
        return toDomain(jpaRepository.save(toEntity(empresa)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Empresa toDomain(EmpresaEntity e) {
        return new Empresa(
                e.getId(),
                e.getIdSimulacion(),
                e.getCodigoEmpresa(),
                e.getNombre(),
                e.getEstrategia(),
                e.getTipoJugador(),
                e.getEstado()
        );
    }

    private EmpresaEntity toEntity(Empresa e) {
        return new EmpresaEntity(
                e.getId(),
                e.getIdSimulacion(),
                e.getCodigoEmpresa(),
                e.getNombre(),
                e.getEstrategia(),
                e.getTipoJugador(),
                e.getEstado()
        );
    }
}