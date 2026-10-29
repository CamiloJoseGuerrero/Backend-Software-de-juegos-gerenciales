package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarEmpresaRequest;
import com.estratego.application.dto.docente.CrearEmpresaRequest;
import com.estratego.domain.model.integrante.Departamento;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import com.estratego.domain.repository.CasoRepository;
import com.estratego.domain.repository.EstadoFinancieroEmpresaRepository;
import com.estratego.application.dto.empresa.HistoriaEmpresaResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private IntegranteRepository integranteRepository;

    @Mock
    private CasoRepository casoRepository;

    @Mock
    private EstadoFinancieroEmpresaRepository estadoFinancieroEmpresaRepository;

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

    private Empresa empresaMulti() {
        return new Empresa(1L, SIMULACION_ID, "EMP-001", "TechStart",
                "Estrategia", TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
    }

    private void contextoEditable(Empresa empresa) {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacionBorrador));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
    }

    @Test
    void noSePuedePasarAMonousuarioConVariosIntegrantes() {
        contextoEditable(empresaMulti());
        when(integranteRepository.findByIdEmpresa(1L)).thenReturn(List.of(
                new Integrante(1L, 1L, 20L, Departamento.COMERCIAL, true),
                new Integrante(2L, 1L, 21L, Departamento.OPERACIONES, false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> empresaService.actualizar(1L,
                        new ActualizarEmpresaRequest("TechStart", null, TipoJugador.MONOUSUARIO), CORREO_DOCENTE));

        assertEquals("No se puede cambiar a MONOUSUARIO: la empresa tiene 2 integrantes y solo admite 1",
                ex.getMessage());
        org.mockito.Mockito.verify(empresaRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void alPasarAMonousuarioElUnicoIntegranteQuedaLider() {
        contextoEditable(empresaMulti());
        Integrante unico = new Integrante(1L, 1L, 20L, Departamento.COMERCIAL, false);
        when(integranteRepository.findByIdEmpresa(1L)).thenReturn(List.of(unico));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = empresaService.actualizar(1L,
                new ActualizarEmpresaRequest("TechStart", null, TipoJugador.MONOUSUARIO), CORREO_DOCENTE);

        assertEquals(TipoJugador.MONOUSUARIO, r.getTipoJugador());
        assertTrue(unico.isEsLider());
        org.mockito.Mockito.verify(integranteRepository).save(unico);
    }

    @Test
    void historiaParaDocenteRechazaSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> empresaService.historiaParaDocente(999L, CORREO_DOCENTE));
    }

    @Test
    void historiaParaDocenteRechazaSiNoEsDelDocente() {
        Empresa e = new Empresa(1L, SIMULACION_ID, "EMP-001", "Alfa", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(e));

        Simulacion ajena = new Simulacion(SIMULACION_ID, 888L, "Ajena", LocalDate.now(), LocalDate.now().plusDays(10), EstadoSimulacion.EN_CURSO);
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(ajena));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        assertThrows(IllegalArgumentException.class,
                () -> empresaService.historiaParaDocente(1L, CORREO_DOCENTE));
    }

    @Test
    void historiaParaDocenteRetornaOrdenadoPorOrdenCasosYOmitiendoSinEstados() {
        Empresa e = new Empresa(1L, SIMULACION_ID, "EMP-001", "Alfa", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(e));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacionBorrador));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        LocalDateTime t1 = LocalDateTime.of(2026, 10, 1, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 10, 8, 8, 0);
        LocalDateTime t3 = LocalDateTime.of(2026, 10, 15, 8, 0);

        Caso c1 = new Caso();
        c1.setId(10L);
        c1.setNombreEmpresa("Caso 1");
        c1.setFechaInicio(t1);

        Caso c2 = new Caso();
        c2.setId(20L);
        c2.setNombreEmpresa("Caso 2");
        c2.setFechaInicio(t2);

        Caso c3SinEstados = new Caso();
        c3SinEstados.setId(30L);
        c3SinEstados.setNombreEmpresa("Caso 3");
        c3SinEstados.setFechaInicio(t3);

        // Devolvemos los casos desordenados para verificar que OrdenCasos los ordena
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(c2, c3SinEstados, c1));

        EstadoFinanciero ef1 = new EstadoFinanciero();
        ef1.setEfectivo(new BigDecimal("1000"));
        EstadoFinancieroEmpresa ee1Inicial = new EstadoFinancieroEmpresa(
                1L, 1L, 10L, MomentoEstado.INICIAL, OrigenEstado.DOCENTE, null, ef1, LocalDateTime.now(), null);

        EstadoFinanciero ef2 = new EstadoFinanciero();
        ef2.setEfectivo(new BigDecimal("1500"));
        EstadoFinancieroEmpresa ee1Final = new EstadoFinancieroEmpresa(
                2L, 1L, 10L, MomentoEstado.FINAL, OrigenEstado.DECISION, 1L, ef2, LocalDateTime.now(), null);

        EstadoFinanciero ef3 = new EstadoFinanciero();
        ef3.setEfectivo(new BigDecimal("1500"));
        EstadoFinancieroEmpresa ee2Inicial = new EstadoFinancieroEmpresa(
                3L, 1L, 20L, MomentoEstado.INICIAL, OrigenEstado.HEREDADO, null, ef3, LocalDateTime.now(), null);

        // Caso 2 solo tiene INICIAL (en curso, final es null)
        when(estadoFinancieroEmpresaRepository.listarPorEmpresa(1L))
                .thenReturn(List.of(ee1Inicial, ee1Final, ee2Inicial));

        List<HistoriaEmpresaResponse> historia = empresaService.historiaParaDocente(1L, CORREO_DOCENTE);

        // Deben ser 2 casos (c1 y c2), omitiendo c3SinEstados
        assertEquals(2, historia.size());

        HistoriaEmpresaResponse h1 = historia.get(0);
        assertEquals(10L, h1.idCaso());
        assertEquals("Caso 1", h1.nombreCaso());
        assertEquals(t1, h1.fechaInicioPartida());
        assertNotNull(h1.inicial());
        assertEquals(new BigDecimal("1000"), h1.inicial().getEfectivo());
        assertNotNull(h1.finalEstado());
        assertEquals(new BigDecimal("1500"), h1.finalEstado().getEfectivo());

        HistoriaEmpresaResponse h2 = historia.get(1);
        assertEquals(20L, h2.idCaso());
        assertEquals("Caso 2", h2.nombreCaso());
        assertEquals(t2, h2.fechaInicioPartida());
        assertNotNull(h2.inicial());
        assertEquals(new BigDecimal("1500"), h2.inicial().getEfectivo());
        assertNull(h2.finalEstado()); // Caso en curso sin final
    }
}