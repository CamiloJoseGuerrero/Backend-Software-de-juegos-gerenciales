package com.estratego.application.usecase;

import com.estratego.application.dto.docente.BoletinCasoRequest;
import com.estratego.application.dto.docente.BoletinCasoResponse;
import com.estratego.domain.model.boletin.BoletinCaso;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.BoletinCasoRepository;
import com.estratego.domain.repository.CasoRepository;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoletinCasoService {

    private static final BigDecimal MIN_INFLACION = new BigDecimal("-100");
    private static final BigDecimal MAX_INFLACION = new BigDecimal("1000");
    private static final BigDecimal MIN_TASA_INTERES = BigDecimal.ZERO;
    private static final BigDecimal MAX_TASA_INTERES = new BigDecimal("1000");

    private final BoletinCasoRepository boletinCasoRepository;
    private final CasoRepository casoRepository;
    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final IntegranteRepository integranteRepository;

    @Transactional
    public BoletinCasoResponse guardarOReemplazar(Long idCaso, BoletinCasoRequest request, String correoDocente) {
        if (idCaso == null) {
            throw new IllegalArgumentException("Debe indicar el caso");
        }
        ContextoDocente ctx = obtenerYValidarDocente(idCaso, correoDocente);

        if (ctx.simulacion().getEstado() == EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException("No se puede modificar el boletín de una simulación finalizada");
        }

        validarNumeros(request);
        String notaNormalizada = normalizarNota(request.getNota());

        LocalDateTime ahora = LocalDateTime.now();
        Optional<BoletinCaso> existente = boletinCasoRepository.findByIdCaso(idCaso);
        BoletinCaso boletin;
        if (existente.isPresent()) {
            boletin = existente.get();
            boletin.setInflacion(request.getInflacion());
            boletin.setTasaInteres(request.getTasaInteres());
            boletin.setTasaCambio(request.getTasaCambio());
            boletin.setNota(notaNormalizada);
            boletin.setActualizadoEn(ahora);
        } else {
            boletin = new BoletinCaso(
                    null,
                    idCaso,
                    request.getInflacion(),
                    request.getTasaInteres(),
                    request.getTasaCambio(),
                    notaNormalizada,
                    ahora,
                    ahora
            );
        }

        BoletinCaso guardado = boletinCasoRepository.save(boletin);
        return toResponse(guardado);
    }

    public Optional<BoletinCasoResponse> obtenerParaDocente(Long idCaso, String correoDocente) {
        if (idCaso == null) {
            throw new IllegalArgumentException("Debe indicar el caso");
        }
        obtenerYValidarDocente(idCaso, correoDocente);
        return boletinCasoRepository.findByIdCaso(idCaso).map(this::toResponse);
    }

    @Transactional
    public void eliminarParaDocente(Long idCaso, String correoDocente) {
        if (idCaso == null) {
            throw new IllegalArgumentException("Debe indicar el caso");
        }
        ContextoDocente ctx = obtenerYValidarDocente(idCaso, correoDocente);

        if (ctx.simulacion().getEstado() == EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException("No se puede eliminar el boletín de una simulación finalizada");
        }

        boletinCasoRepository.deleteByIdCaso(idCaso);
    }

    public Optional<BoletinCasoResponse> obtenerParaEstudiante(Long idCaso, String correoEstudiante) {
        if (idCaso == null) {
            throw new IllegalArgumentException("Debe indicar el caso");
        }

        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new IllegalArgumentException("Caso no encontrado"));

        Usuario estudiante = usuarioRepository.findByCorreo(correoEstudiante)
                .orElseThrow(() -> new InvalidCredentialsException("Estudiante no encontrado"));

        if (!integranteRepository.existsEnSimulacion(caso.getIdSimulacion(), estudiante.getId())) {
            throw new IllegalArgumentException("El caso no pertenece a una simulación del estudiante");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (caso.getFechaVisualizacion() != null && ahora.isBefore(caso.getFechaVisualizacion())) {
            return Optional.empty();
        }

        return boletinCasoRepository.findByIdCaso(idCaso).map(this::toResponse);
    }

    // --- Helpers de validación y mapeo ---

    private ContextoDocente obtenerYValidarDocente(Long idCaso, String correoDocente) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new IllegalArgumentException("Caso no encontrado"));

        Simulacion simulacion = simulacionRepository.findById(caso.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        Usuario docente = usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"));

        if (!docente.getId().equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("El caso no pertenece a una simulación de este docente");
        }

        return new ContextoDocente(caso, simulacion, docente);
    }

    private void validarNumeros(BoletinCasoRequest r) {
        if (r.getInflacion() == null) {
            throw new CampoInvalidoException("inflacion", "La inflación es obligatoria");
        }
        if (r.getInflacion().compareTo(MIN_INFLACION) < 0 || r.getInflacion().compareTo(MAX_INFLACION) > 0) {
            throw new CampoInvalidoException("inflacion", "La inflación va de -100 a 1000");
        }

        if (r.getTasaInteres() == null) {
            throw new CampoInvalidoException("tasaInteres", "La tasa de interés es obligatoria");
        }
        if (r.getTasaInteres().compareTo(MIN_TASA_INTERES) < 0 || r.getTasaInteres().compareTo(MAX_TASA_INTERES) > 0) {
            throw new CampoInvalidoException("tasaInteres", "La tasa de interés va de 0 a 1000");
        }

        if (r.getTasaCambio() == null) {
            throw new CampoInvalidoException("tasaCambio", "La tasa de cambio es obligatoria");
        }
        if (r.getTasaCambio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CampoInvalidoException("tasaCambio", "La tasa de cambio tiene que ser mayor que 0");
        }
    }

    private String normalizarNota(String nota) {
        if (nota == null) {
            return null;
        }
        String limpia = nota.trim();
        if (limpia.isEmpty()) {
            return null;
        }
        if (limpia.length() > 2000) {
            throw new CampoInvalidoException("nota", "La nota no puede superar los 2000 caracteres");
        }
        return limpia;
    }

    private BoletinCasoResponse toResponse(BoletinCaso b) {
        return new BoletinCasoResponse(
                b.getIdCaso(),
                b.getInflacion(),
                b.getTasaInteres(),
                b.getTasaCambio(),
                b.getNota(),
                b.getPublicadoEn(),
                b.getActualizadoEn()
        );
    }

    private record ContextoDocente(Caso caso, Simulacion simulacion, Usuario docente) {
    }
}
