package com.estratego.application.usecase;

import com.estratego.application.dto.docente.CrearPartidaRequest;
import com.estratego.domain.model.partida.EstadoPartida;
import com.estratego.domain.model.partida.Partida;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.PartidaRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.estratego.domain.repository.EquipoRepository;
import com.estratego.application.dto.docente.AsignarEquiposRequest;
import com.estratego.domain.model.equipo.Equipo;
import java.util.List;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @InjectMocks
    private PartidaService partidaService;

    private Usuario docente;

    @BeforeEach
    void setUp() {
        docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE, "DOC", "hash", Rol.DOCENTE, null);
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
    }

    @Test
    void crearPartidaCalculaFechaCierre() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        CrearPartidaRequest request = new CrearPartidaRequest(1L, inicio, 60, null);

        when(partidaRepository.save(any(Partida.class))).thenAnswer(inv -> {
            Partida p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var response = partidaService.crear(request, CORREO_DOCENTE);

        assertEquals(EstadoPartida.CONFIGURADA, response.getEstado());
        assertEquals(inicio.plusMinutes(60), response.getFechaHoraCierre());
    }

    @Test
    void crearPartidaRechazaFechaInicioEnElPasado() {
        CrearPartidaRequest request = new CrearPartidaRequest(
                1L, LocalDateTime.now().minusHours(1), 60, null);

        assertThrows(IllegalArgumentException.class,
                () -> partidaService.crear(request, CORREO_DOCENTE));
    }

    @Test
    void crearPartidaRechazaDuracionCero() {
        CrearPartidaRequest request = new CrearPartidaRequest(
                1L, LocalDateTime.now().plusDays(1), 0, null);

        assertThrows(IllegalArgumentException.class,
                () -> partidaService.crear(request, CORREO_DOCENTE));
    }

    @Test
    void programarCambiaEstado() {
        Partida partida = new Partida(1L, 1L, DOCENTE_ID,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
                60, null, EstadoPartida.CONFIGURADA, new ArrayList<>());

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = partidaService.programar(1L, CORREO_DOCENTE);

        assertEquals(EstadoPartida.PROGRAMADA, response.getEstado());
    }

    @Test
    void iniciarRechazaSiAunNoEsLaFecha() {
        Partida partida = new Partida(1L, 1L, DOCENTE_ID,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
                60, null, EstadoPartida.PROGRAMADA, new ArrayList<>());

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));

        assertThrows(IllegalArgumentException.class,
                () -> partidaService.iniciar(1L, CORREO_DOCENTE));
    }

    @Test
    void iniciarCambiaEstadoSiYaEsLaFecha() {
        Partida partida = new Partida(1L, 1L, DOCENTE_ID,
                LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(1),
                60, null, EstadoPartida.PROGRAMADA, new ArrayList<>());

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = partidaService.iniciar(1L, CORREO_DOCENTE);

        assertEquals(EstadoPartida.EN_CURSO, response.getEstado());
    }

    @Test
    void finalizarSoloDesdeEnCurso() {
        Partida partida = new Partida(1L, 1L, DOCENTE_ID,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                60, null, EstadoPartida.CONFIGURADA, new ArrayList<>());

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));

        assertThrows(IllegalArgumentException.class,
                () -> partidaService.finalizar(1L, CORREO_DOCENTE));
    }



@Test
void asignarEquiposExitoso() {
    Partida partida = new Partida(1L, 1L, DOCENTE_ID,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
            60, null, EstadoPartida.CONFIGURADA, new ArrayList<>());

    Equipo equipo1 = new Equipo(1L, "Equipo 1", DOCENTE_ID, 1L, List.of(1L, 2L));

    when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
    when(equipoRepository.findByIdIn(List.of(1L))).thenReturn(List.of(equipo1));
    when(partidaRepository.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));

    var response = partidaService.asignarEquipos(1L,
            new AsignarEquiposRequest(List.of(1L)), CORREO_DOCENTE);

    assertEquals(1, response.getEquipos().size());
    assertEquals(1L, response.getPartidaId());
}

@Test
void asignarEquiposRechazaPartidaEnCurso() {
    Partida partida = new Partida(1L, 1L, DOCENTE_ID,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
            60, null, EstadoPartida.EN_CURSO, new ArrayList<>());

    when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));

    assertThrows(IllegalArgumentException.class,
            () -> partidaService.asignarEquipos(1L,
                    new AsignarEquiposRequest(List.of(1L)), CORREO_DOCENTE));
}

@Test
void asignarEquiposRechazaEquipoDeOtroDocente() {
    Partida partida = new Partida(1L, 1L, DOCENTE_ID,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
            60, null, EstadoPartida.CONFIGURADA, new ArrayList<>());

    Equipo equipoAjeno = new Equipo(1L, "Equipo 1", 999L, 1L, List.of(1L));

    when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
    when(equipoRepository.findByIdIn(List.of(1L))).thenReturn(List.of(equipoAjeno));

    assertThrows(IllegalArgumentException.class,
            () -> partidaService.asignarEquipos(1L,
                    new AsignarEquiposRequest(List.of(1L)), CORREO_DOCENTE));
}

@Test
void asignarEquiposRechazaEquipoEnOtraPartidaEnCurso() {
    Partida partida = new Partida(1L, 1L, DOCENTE_ID,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
            60, null, EstadoPartida.CONFIGURADA, new ArrayList<>());

    Equipo equipo = new Equipo(1L, "Equipo 1", DOCENTE_ID, 1L, List.of(1L));

    when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
    when(equipoRepository.findByIdIn(List.of(1L))).thenReturn(List.of(equipo));
    when(partidaRepository.existeEquipoEnPartidaEnCurso(1L, 1L)).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
            () -> partidaService.asignarEquipos(1L,
                    new AsignarEquiposRequest(List.of(1L)), CORREO_DOCENTE));
}

@Test
void quitarEquipoExitoso() {
    Partida partida = new Partida(1L, 1L, DOCENTE_ID,
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1),
            60, null, EstadoPartida.CONFIGURADA, new ArrayList<>(List.of(1L)));

    when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
    when(equipoRepository.findByIdIn(any())).thenReturn(List.of());
    when(partidaRepository.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));

    var response = partidaService.quitarEquipo(1L, 1L, CORREO_DOCENTE);

    assertTrue(response.getEquipos().isEmpty());
}

}