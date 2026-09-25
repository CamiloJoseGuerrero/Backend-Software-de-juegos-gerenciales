package com.estratego.application.usecase;

import com.estratego.application.dto.docente.CrearEmpresaRequest;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;
    private static final Long SIMULACION_ID = 10L;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private SimulacionRepository simulacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmpresaService empresaService;

    private Usuario docente;
    private Simulacion simulacionBorrador;

    @BeforeEach
    void setUp() {
        docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE,
                "DOC", "docente", "hash", Rol.DOCENTE);

        simulacionBorrador = new Simulacion(SIMULACION_ID, DOCENTE_ID, "Simulación Test",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                EstadoSimulacion.BORRADOR);
    }

    @Test
    void crearEmpresaGeneraCodigoAutomatico() {
        CrearEmpresaRequest request = new CrearEmpresaRequest(
                "TechStart S.A.", "Estrategia agresiva", TipoJugador.MULTIUSUARIO);

        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacionBorrador));
        when(empresaRepository.countByIdSimulacion(SIMULACION_ID)).thenReturn(0L);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> {
            Empresa e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        var response = empresaService.crear(SIMULACION_ID, request, CORREO_DOCENTE);

        assertEquals("EMP-001", response.getCodigoEmpresa());
        assertEquals("TechStart S.A.", response.getNombre());
        assertEquals(TipoJugador.MULTIUSUARIO, response.getTipoJugador());
        assertEquals(EstadoEmpresa.ACTIVA, response.getEstado());
    }

    @Test
    void crearEmpresaRechazaSiSimulacionEnCurso() {
        simulacionBorrador.setEstado(EstadoSimulacion.EN_CURSO);
        CrearEmpresaRequest request = new CrearEmpresaRequest(
                "TechStart S.A.", "Estrategia agresiva", TipoJugador.MULTIUSUARIO);

        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacionBorrador));

        assertThrows(IllegalArgumentException.class,
                () -> empresaService.crear(SIMULACION_ID, request, CORREO_DOCENTE));
    }

    @Test
    void crearEmpresaRechazaSiSimulacionDeOtroDocente() {
        Simulacion otraSimulacion = new Simulacion(SIMULACION_ID, 888L, "Otra",
                LocalDate.now().plusDays(1), null, EstadoSimulacion.BORRADOR);

        CrearEmpresaRequest request = new CrearEmpresaRequest(
                "TechStart S.A.", "Estrategia", TipoJugador.MULTIUSUARIO);

        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(otraSimulacion));

        assertThrows(IllegalArgumentException.class,
                () -> empresaService.crear(SIMULACION_ID, request, CORREO_DOCENTE));
    }

    @Test
    void eliminarEmpresaRechazaSiSimulacionEnCurso() {
        Empresa empresa = new Empresa(1L, SIMULACION_ID, "EMP-001", "TechStart",
                "Estrategia", TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);

        Simulacion enCurso = new Simulacion(SIMULACION_ID, DOCENTE_ID, "Sim",
                LocalDate.now(), LocalDate.now().plusDays(30), EstadoSimulacion.EN_CURSO);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(enCurso));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        assertThrows(IllegalArgumentException.class,
                () -> empresaService.eliminar(1L, CORREO_DOCENTE));
    }
}