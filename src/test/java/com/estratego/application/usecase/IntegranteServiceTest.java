package com.estratego.application.usecase;

import com.estratego.application.dto.docente.AgregarIntegranteRequest;
import com.estratego.application.dto.docente.IntegranteResponse;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.integrante.Departamento;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegranteServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;
    private static final Long SIMULACION_ID = 10L;
    private static final Long EMPRESA_ID = 5L;
    private static final Long ESTUDIANTE_ID = 20L;

    @Mock private IntegranteRepository integranteRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private SimulacionRepository simulacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DocenteEstudianteRepository docenteEstudianteRepository;

    @InjectMocks
    private IntegranteService integranteService;

    private Usuario docente;
    private Usuario estudiante;
    private Empresa empresa;
    private Simulacion simulacion;

    @BeforeEach
    void setUp() {
        docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE, "DOC", "docente", "hash", Rol.DOCENTE);
        estudiante = new Usuario(ESTUDIANTE_ID, "Ana", "ana@test.com", "123", "ana", "hash", Rol.ESTUDIANTE);
        empresa = new Empresa(EMPRESA_ID, SIMULACION_ID, "EMP-001", "TechStart", null,
                TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
        simulacion = new Simulacion(SIMULACION_ID, DOCENTE_ID, "Simulación",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), EstadoSimulacion.BORRADOR);
    }

    private void contextoValido() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        // El estudiante de prueba fue cargado por este docente
        lenient().when(docenteEstudianteRepository.existeVinculo(DOCENTE_ID, ESTUDIANTE_ID)).thenReturn(true);
    }

    private void guardarDevuelveConId() {
        when(integranteRepository.save(any(Integrante.class))).thenAnswer(inv -> {
            Integrante i = inv.getArgument(0);
            if (i.getId() == null) i.setId(1L);
            return i;
        });
    }

    @Test
    void agregarEstudianteConDepartamentoPorDefecto() {
        contextoValido();
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(false);
        guardarDevuelveConId();

        IntegranteResponse r = integranteService.agregar(EMPRESA_ID,
                new AgregarIntegranteRequest(ESTUDIANTE_ID, null, null), CORREO_DOCENTE);

        assertEquals(Departamento.GERENCIA_GENERAL, r.getDepartamento());
        assertFalse(r.isEsLider());
        assertEquals("Ana", r.getNombre());
        verify(integranteRepository, never()).quitarLider(any());
    }

    @Test
    void agregarComoLiderReemplazaAlLiderAnterior() {
        contextoValido();
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(false);
        guardarDevuelveConId();

        IntegranteResponse r = integranteService.agregar(EMPRESA_ID,
                new AgregarIntegranteRequest(ESTUDIANTE_ID, Departamento.COMERCIAL, true), CORREO_DOCENTE);

        assertTrue(r.isEsLider());
        InOrder orden = inOrder(integranteRepository);
        orden.verify(integranteRepository).quitarLider(EMPRESA_ID);
        orden.verify(integranteRepository).save(any(Integrante.class));
    }

    @Test
    void rechazaUsuarioQueNoEsEstudiante() {
        contextoValido();
        when(usuarioRepository.findById(DOCENTE_ID)).thenReturn(Optional.of(docente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                integranteService.agregar(EMPRESA_ID,
                        new AgregarIntegranteRequest(DOCENTE_ID, null, null), CORREO_DOCENTE));

        assertEquals("El usuario no es un estudiante", ex.getMessage());
        verify(integranteRepository, never()).save(any());
    }

    @Test
    void rechazaEstudianteDeOtroDocente() {
        contextoValido();
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudiante));
        when(docenteEstudianteRepository.existeVinculo(DOCENTE_ID, ESTUDIANTE_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                integranteService.agregar(EMPRESA_ID,
                        new AgregarIntegranteRequest(ESTUDIANTE_ID, null, null), CORREO_DOCENTE));

        assertEquals("El estudiante no está en tu lista de estudiantes", ex.getMessage());
        verify(integranteRepository, never()).save(any());
    }

    @Test
    void rechazaEstudianteQueYaEstaEnOtraEmpresaDeLaSimulacion() {
        contextoValido();
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                integranteService.agregar(EMPRESA_ID,
                        new AgregarIntegranteRequest(ESTUDIANTE_ID, null, null), CORREO_DOCENTE));
        verify(integranteRepository, never()).save(any());
    }

    @Test
    void monousuarioAceptaSoloUnoYLoHaceLider() {
        empresa.setTipoJugador(TipoJugador.MONOUSUARIO);
        contextoValido();
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(false);
        when(integranteRepository.countByIdEmpresa(EMPRESA_ID)).thenReturn(0L);
        guardarDevuelveConId();

        IntegranteResponse r = integranteService.agregar(EMPRESA_ID,
                new AgregarIntegranteRequest(ESTUDIANTE_ID, null, false), CORREO_DOCENTE);
        assertTrue(r.isEsLider());

        when(integranteRepository.countByIdEmpresa(EMPRESA_ID)).thenReturn(1L);
        Usuario otro = new Usuario(21L, "Luis", "luis@test.com", "456", "luis", "hash", Rol.ESTUDIANTE);
        when(usuarioRepository.findById(21L)).thenReturn(Optional.of(otro));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, 21L)).thenReturn(false);
        when(docenteEstudianteRepository.existeVinculo(DOCENTE_ID, 21L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                integranteService.agregar(EMPRESA_ID,
                        new AgregarIntegranteRequest(21L, null, null), CORREO_DOCENTE));
        assertEquals("Una empresa MONOUSUARIO solo puede tener un integrante", ex.getMessage());
    }

    @Test
    void rechazaCambiosSiLaSimulacionEstaEnCurso() {
        simulacion.setEstado(EstadoSimulacion.EN_CURSO);
        contextoValido();

        assertThrows(IllegalArgumentException.class, () ->
                integranteService.agregar(EMPRESA_ID,
                        new AgregarIntegranteRequest(ESTUDIANTE_ID, null, null), CORREO_DOCENTE));
        assertThrows(IllegalArgumentException.class, () ->
                integranteService.quitar(EMPRESA_ID, ESTUDIANTE_ID, CORREO_DOCENTE));
    }

    @Test
    void rechazaEmpresaDeOtroDocente() {
        simulacion.setIdUsuarioCoordinador(1234L);
        contextoValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                integranteService.listar(EMPRESA_ID, CORREO_DOCENTE));
        assertEquals("La empresa no pertenece a una simulación de este docente", ex.getMessage());
    }

    @Test
    void asignarLiderQuitaElAnteriorYMarcaAlNuevo() {
        contextoValido();
        Integrante integrante = new Integrante(7L, EMPRESA_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, false);
        when(integranteRepository.findByIdEmpresaAndIdUsuario(EMPRESA_ID, ESTUDIANTE_ID))
                .thenReturn(Optional.of(integrante));
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(estudiante));
        guardarDevuelveConId();

        IntegranteResponse r = integranteService.asignarLider(EMPRESA_ID, ESTUDIANTE_ID, CORREO_DOCENTE);

        assertTrue(r.isEsLider());
        InOrder orden = inOrder(integranteRepository);
        orden.verify(integranteRepository).quitarLider(EMPRESA_ID);
        orden.verify(integranteRepository).save(integrante);
    }

    @Test
    void quitarIntegranteQueNoExisteFalla() {
        contextoValido();
        when(integranteRepository.findByIdEmpresaAndIdUsuario(EMPRESA_ID, ESTUDIANTE_ID))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                integranteService.quitar(EMPRESA_ID, ESTUDIANTE_ID, CORREO_DOCENTE));
        verify(integranteRepository, never()).deleteById(any());
    }

    @Test
    void quitarIntegranteLoElimina() {
        contextoValido();
        Integrante integrante = new Integrante(7L, EMPRESA_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, false);
        when(integranteRepository.findByIdEmpresaAndIdUsuario(EMPRESA_ID, ESTUDIANTE_ID))
                .thenReturn(Optional.of(integrante));

        integranteService.quitar(EMPRESA_ID, ESTUDIANTE_ID, CORREO_DOCENTE);

        verify(integranteRepository).deleteById(7L);
    }
}
