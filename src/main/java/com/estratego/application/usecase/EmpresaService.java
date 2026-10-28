package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarEmpresaRequest;
import com.estratego.application.dto.docente.CrearEmpresaRequest;
import com.estratego.application.dto.docente.EmpresaResponse;
import com.estratego.application.dto.docente.EstadoFinancieroEmpresaResponse;
import com.estratego.application.dto.empresa.HistoriaEmpresaResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.OrdenCasos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final IntegranteRepository integranteRepository;
    private final CasoRepository casoRepository;
    private final EstadoFinancieroEmpresaRepository estadoFinancieroEmpresaRepository;

    @Transactional
    public EmpresaResponse crear(Long idSimulacion, CrearEmpresaRequest request, String correoDocente) {
        Simulacion simulacion = obtenerSimulacionYValidar(idSimulacion, correoDocente);

        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR
                && simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden crear empresas en simulaciones BORRADOR o PROGRAMADA");
        }

        long siguiente = empresaRepository.countByIdSimulacion(idSimulacion) + 1;
        String codigo = String.format("EMP-%03d", siguiente);

        Empresa empresa = new Empresa(
                null,
                idSimulacion,
                codigo,
                request.getNombre().trim(),
                request.getEstrategia(),
                request.getTipoJugador() != null ? request.getTipoJugador() : TipoJugador.MULTIUSUARIO,
                EstadoEmpresa.ACTIVA
        );

        return toResponse(empresaRepository.save(empresa));
    }

    public List<EmpresaResponse> listarPorSimulacion(Long idSimulacion, String correoDocente) {
        obtenerSimulacionYValidar(idSimulacion, correoDocente);

        return empresaRepository.findByIdSimulacion(idSimulacion).stream()
                .map(this::toResponse)
                .toList();
    }

    public EmpresaResponse obtener(Long id, String correoDocente) {
        Empresa empresa = obtenerYValidar(id, correoDocente);
        return toResponse(empresa);
    }

    /**
     * Historia financiera de la empresa a lo largo de los casos jugados de la simulación.
     */
    @Transactional(readOnly = true)
    public List<HistoriaEmpresaResponse> historiaParaDocente(Long idEmpresa, String correoDocente) {
        Empresa empresa = obtenerYValidar(idEmpresa, correoDocente);
        return construirHistoria(empresa);
    }

    private List<HistoriaEmpresaResponse> construirHistoria(Empresa empresa) {
        List<EstadoFinancieroEmpresa> estados = estadoFinancieroEmpresaRepository.listarPorEmpresa(empresa.getId());
        Map<Long, Map<MomentoEstado, EstadoFinancieroEmpresa>> porCaso = estados.stream()
                .collect(Collectors.groupingBy(
                        EstadoFinancieroEmpresa::getIdCaso,
                        Collectors.toMap(EstadoFinancieroEmpresa::getMomento, Function.identity(), (a, b) -> b)
                ));

        List<Caso> casosOrdenados = OrdenCasos.ordenados(casoRepository.findByIdSimulacion(empresa.getIdSimulacion()));
        List<HistoriaEmpresaResponse> resultado = new ArrayList<>();
        for (Caso caso : casosOrdenados) {
            Map<MomentoEstado, EstadoFinancieroEmpresa> momentos = porCaso.get(caso.getId());
            if (momentos == null || momentos.isEmpty()) {
                continue;
            }
            EstadoFinancieroEmpresa inicial = momentos.get(MomentoEstado.INICIAL);
            EstadoFinancieroEmpresa finalEstado = momentos.get(MomentoEstado.FINAL);

            resultado.add(new HistoriaEmpresaResponse(
                    caso.getId(),
                    caso.getNombreEmpresa(),
                    caso.getFechaInicio(),
                    EstadoFinancieroEmpresaResponse.de(inicial),
                    EstadoFinancieroEmpresaResponse.de(finalEstado)
            ));
        }
        return resultado;
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, ActualizarEmpresaRequest request, String correoDocente) {
        Empresa empresa = obtenerYValidar(id, correoDocente);

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (simulacion.getEstado() == EstadoSimulacion.EN_CURSO
                || simulacion.getEstado() == EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException(
                    "No se pueden editar empresas de simulaciones en curso o finalizadas");
        }

        empresa.setNombre(request.getNombre().trim());
        empresa.setEstrategia(request.getEstrategia());
        if (request.getTipoJugador() != null) {
            if (request.getTipoJugador() == TipoJugador.MONOUSUARIO
                    && empresa.getTipoJugador() != TipoJugador.MONOUSUARIO) {
                pasarAMonousuario(empresa);
            }
            empresa.setTipoJugador(request.getTipoJugador());
        }

        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public void eliminar(Long id, String correoDocente) {
        Empresa empresa = obtenerYValidar(id, correoDocente);

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR
                && simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden eliminar empresas de simulaciones BORRADOR o PROGRAMADA");
        }

        integranteRepository.deleteByIdEmpresa(id);
        empresaRepository.deleteById(id);
    }

    /** MONOUSUARIO admite un solo integrante, y ese integrante es el líder. */
    private void pasarAMonousuario(Empresa empresa) {
        List<Integrante> integrantes = integranteRepository.findByIdEmpresa(empresa.getId());
        if (integrantes.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "No se puede cambiar a MONOUSUARIO: la empresa tiene %d integrantes y solo admite 1",
                    integrantes.size()));
        }
        if (integrantes.size() == 1 && !integrantes.get(0).isEsLider()) {
            Integrante unico = integrantes.get(0);
            unico.setEsLider(true);
            integranteRepository.save(unico);
        }
    }

    private Empresa obtenerYValidar(Long id, String correoDocente) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        Long idDocente = resolverDocenteId(correoDocente);

        if (!idDocente.equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La empresa no pertenece a una simulación de este docente");
        }

        return empresa;
    }

    private Simulacion obtenerSimulacionYValidar(Long idSimulacion, String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);

        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (!idDocente.equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La simulación no pertenece a este docente");
        }

        return simulacion;
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }

    private EmpresaResponse toResponse(Empresa e) {
        return new EmpresaResponse(
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