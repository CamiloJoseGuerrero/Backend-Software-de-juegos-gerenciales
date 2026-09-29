package com.estratego.infrastructure.persistence.adapter;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.repository.CasoRepository;
import com.estratego.infrastructure.persistence.entity.CasoEntity;
import com.estratego.infrastructure.persistence.entity.CasoOpcionEntity;
import com.estratego.infrastructure.persistence.repository.CasoJpaRepository;
import com.estratego.infrastructure.persistence.repository.CasoOpcionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CasoRepositoryAdapter implements CasoRepository {

    private final CasoJpaRepository casoJpa;
    private final CasoOpcionJpaRepository opcionJpa;

    @Override
    public Optional<Caso> findById(Long id) {
        return casoJpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Caso> findByIdSimulacion(Long idSimulacion) {
        return casoJpa.findByIdSimulacionOrderByFechaInicioAsc(idSimulacion).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Caso save(Caso caso) {
        CasoEntity guardado = casoJpa.save(toEntity(caso));

        opcionJpa.deleteByIdCaso(guardado.getId());
        List<CasoOpcionEntity> opciones = new ArrayList<>();
        int orden = 1;
        for (OpcionCaso o : caso.getOpciones()) {
            opciones.add(new CasoOpcionEntity(null, guardado.getId(), orden++, o.getOpcion(), o.getResultado()));
        }
        opcionJpa.saveAll(opciones);

        return toDomain(guardado);
    }

    @Override
    public List<Caso> findByIdSimulacionIn(List<Long> idsSimulacion) {
        if (idsSimulacion.isEmpty()) return List.of();
        return casoJpa.findByIdSimulacionInOrderByFechaInicioAsc(idsSimulacion).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Caso> findActivoBySimulacion(Long idSimulacion) {
        return casoJpa.findFirstByIdSimulacionAndEstado(idSimulacion, EstadoCaso.ACTIVO).map(this::toDomain);
    }

    @Override
    public void desactivarTodos(Long idSimulacion) {
        casoJpa.desactivarTodos(idSimulacion);
    }

    @Override
    public void marcarActivo(Long id) {
        casoJpa.marcarActivo(id);
    }

    @Override
    public void deleteById(Long id) {
        opcionJpa.deleteByIdCaso(id);
        casoJpa.deleteById(id);
    }

    private Caso toDomain(CasoEntity e) {
        List<OpcionCaso> opciones = opcionJpa.findByIdCasoOrderByOrdenAsc(e.getId()).stream()
                .map(o -> new OpcionCaso(o.getId(), o.getOrden(), o.getOpcion(), o.getResultado()))
                .toList();
        return new Caso(
                e.getId(), e.getIdSimulacion(),
                e.getNombreEmpresa(), e.getMision(), e.getVision(), e.getTipo(),
                e.getActivoTotal(), e.getPasivoTotal(), e.getPatrimonio(), e.getUtilidadNeta(),
                e.getPenalizacionMin(), e.getPenalizacionMax(),
                e.getFechaVisualizacion(), e.getFechaInicio(), e.getFechaFin(),
                new ArrayList<>(opciones),
                e.getEstado(), e.getAsignacionEquipos()
        );
    }

    private CasoEntity toEntity(Caso c) {
        return new CasoEntity(
                c.getId(), c.getIdSimulacion(),
                c.getNombreEmpresa(), c.getMision(), c.getVision(), c.getTipo(),
                c.getActivoTotal(), c.getPasivoTotal(), c.getPatrimonio(), c.getUtilidadNeta(),
                c.getPenalizacionMin(), c.getPenalizacionMax(),
                c.getFechaVisualizacion(), c.getFechaInicio(), c.getFechaFin(),
                c.getEstado(), c.getAsignacionEquipos()
        );
    }
}
