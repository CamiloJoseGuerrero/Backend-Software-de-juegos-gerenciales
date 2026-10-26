package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.boletin.BoletinCaso;
import com.estratego.domain.repository.BoletinCasoRepository;
import com.estratego.infrastructure.persistence.entity.BoletinCasoEntity;
import com.estratego.infrastructure.persistence.repository.BoletinCasoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BoletinCasoRepositoryAdapter implements BoletinCasoRepository {

    private final BoletinCasoJpaRepository jpaRepository;

    @Override
    public Optional<BoletinCaso> findByIdCaso(Long idCaso) {
        return jpaRepository.findByIdCaso(idCaso).map(this::toDomain);
    }

    @Override
    public BoletinCaso save(BoletinCaso boletin) {
        BoletinCasoEntity entity = toEntity(boletin);
        BoletinCasoEntity guardada = jpaRepository.save(entity);
        return toDomain(guardada);
    }

    @Override
    public void deleteByIdCaso(Long idCaso) {
        jpaRepository.findByIdCaso(idCaso).ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsByIdCaso(Long idCaso) {
        return jpaRepository.existsByIdCaso(idCaso);
    }

    private BoletinCaso toDomain(BoletinCasoEntity e) {
        return new BoletinCaso(
                e.getId(),
                e.getIdCaso(),
                e.getInflacion(),
                e.getTasaInteres(),
                e.getTasaCambio(),
                e.getNota(),
                e.getPublicadoEn(),
                e.getActualizadoEn()
        );
    }

    private BoletinCasoEntity toEntity(BoletinCaso b) {
        return new BoletinCasoEntity(
                b.getId(),
                b.getIdCaso(),
                b.getInflacion(),
                b.getTasaInteres(),
                b.getTasaCambio(),
                b.getNota(),
                b.getPublicadoEn(),
                b.getActualizadoEn()
        );
    }
}
