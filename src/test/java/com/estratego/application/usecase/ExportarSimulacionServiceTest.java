package com.estratego.application.usecase;

import com.estratego.application.dto.clasificacion.ClasificacionResponse;
import com.estratego.application.dto.clasificacion.FilaClasificacionResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportarSimulacionServiceTest {

    private static final Long SIM = 10L;
    private static final String DOCENTE = "docente@test.com";

    @Mock private ClasificacionService clasificacionService;
    @Mock private CasoRepository casoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private DecisionCasoRepository decisionCasoRepository;
    @Mock private EstadoFinancieroEmpresaRepository estadoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ExportarSimulacionService service;

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    private static EstadoFinanciero balance() {
        return new EstadoFinanciero(
                bd("200000"), bd("150000"), bd("250000"), bd("350000"), bd("50000"),
                bd("150000"), bd("100000"), bd("150000"),
                bd("400000"), bd("100000"),
                bd("800000"), bd("500000"), bd("80000"), bd("60000"), bd("20000"), bd("40000"),
                bd("120000"), bd("-80000"), bd("-20000"));
    }

    @Test
    void generaLasTresHojasConLosDatosDeLaSimulacion() throws Exception {
        when(clasificacionService.paraDocente(SIM, DOCENTE)).thenReturn(new ClasificacionResponse(
                SIM, "Gerencia 2026-2", EstadoSimulacion.FINALIZADA, true, 1, List.of(
                new FilaClasificacionResponse(1, 1L, "EMP-001", "Alfa", bd("190000"), 0, List.of()),
                new FilaClasificacionResponse(2, 2L, "EMP-002", "Beta", bd("92000"), 1, List.of()))));
        Caso caso = new Caso();
        caso.setId(7L);
        caso.setIdSimulacion(SIM);
        caso.setNombreEmpresa("TextilAndes");
        caso.setFechaInicio(LocalDateTime.of(2026, 10, 20, 8, 0));
        caso.setActivadoEn(LocalDateTime.of(2026, 10, 20, 8, 0));
        caso.setOpciones(new ArrayList<>(List.of(new OpcionCaso(71L, 1, "Ampliar", "Vende más", null))));
        Caso nuncaActivado = new Caso();
        nuncaActivado.setId(8L);
        nuncaActivado.setFechaInicio(LocalDateTime.of(2026, 10, 21, 8, 0));
        when(casoRepository.findByIdSimulacion(SIM)).thenReturn(List.of(caso, nuncaActivado));
        when(empresaRepository.findByIdSimulacion(SIM)).thenReturn(List.of(
                new Empresa(2L, SIM, "EMP-002", "Beta", null, TipoJugador.MONOUSUARIO, EstadoEmpresa.ACTIVA),
                new Empresa(1L, SIM, "EMP-001", "Alfa", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA)));
        when(estadoRepository.listarPorCaso(7L)).thenReturn(List.of(
                new EstadoFinancieroEmpresa(2L, 1L, 7L, MomentoEstado.FINAL, OrigenEstado.DECISION, 71L, balance(), null),
                new EstadoFinancieroEmpresa(1L, 1L, 7L, MomentoEstado.INICIAL, OrigenEstado.DOCENTE, null, balance(), null),
                new EstadoFinancieroEmpresa(3L, 2L, 7L, MomentoEstado.FINAL, OrigenEstado.PENALIZACION, null, balance(), null,
                        bd("22.58"))));
        when(decisionCasoRepository.findByIdCaso(7L)).thenReturn(List.of(
                new DecisionCaso(1L, 7L, 1L, 71L, 20L, LocalDateTime.of(2026, 10, 20, 9, 30))));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(
                new Usuario(20L, "Ana Pérez", "ana@test.com", "1", "ana", "h", Rol.ESTUDIANTE)));

        ExportarSimulacionService.Archivo archivo = service.exportar(SIM, DOCENTE);

        assertEquals("estratego-simulacion-10.xlsx", archivo.nombre());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(archivo.contenido()))) {
            assertEquals(List.of("Clasificación", "Estados por empresa", "Decisiones"),
                    List.of(wb.getSheetName(0), wb.getSheetName(1), wb.getSheetName(2)));

            Sheet clas = wb.getSheet("Clasificación");
            assertEquals("Gerencia 2026-2", clas.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Alfa", clas.getRow(4).getCell(2).getStringCellValue());
            assertEquals(190000, clas.getRow(4).getCell(3).getNumericCellValue());
            assertEquals(1, clas.getRow(5).getCell(4).getNumericCellValue());

            Sheet est = wb.getSheet("Estados por empresa");
            assertEquals("Inicial", est.getRow(1).getCell(3).getStringCellValue()); // inicial antes que final
            assertEquals("docente", est.getRow(1).getCell(4).getStringCellValue());
            assertEquals("decision", est.getRow(2).getCell(4).getStringCellValue());
            assertEquals(200000, est.getRow(1).getCell(5).getNumericCellValue());   // efectivo
            assertEquals(1000000, est.getRow(1).getCell(24).getNumericCellValue()); // activo total
            assertEquals(2.4, est.getRow(1).getCell(28).getNumericCellValue(), 1e-9); // razón corriente
            assertEquals("penalizacion", est.getRow(3).getCell(4).getStringCellValue()); // EMP-002 después de EMP-001
            assertNull(est.getRow(4)); // el caso nunca activado no aparece

            Sheet dec = wb.getSheet("Decisiones");
            assertEquals("EMP-001", dec.getRow(1).getCell(1).getStringCellValue()); // ordenadas por código
            assertEquals("Sí", dec.getRow(1).getCell(3).getStringCellValue());
            assertEquals("Ampliar", dec.getRow(1).getCell(4).getStringCellValue());
            assertEquals("Ana Pérez", dec.getRow(1).getCell(6).getStringCellValue());
            assertEquals("No", dec.getRow(2).getCell(3).getStringCellValue());
            assertEquals(22.58, dec.getRow(2).getCell(8).getNumericCellValue(), 1e-9); // % sorteado
            assertNull(dec.getRow(1).getCell(8)); // quien decidió no tiene penalización
        }
        verify(estadoRepository, never()).listarPorCaso(8L);
    }

    @Test
    void siLaSimulacionNoEsDelDocenteNoGeneraNada() {
        when(clasificacionService.paraDocente(SIM, DOCENTE))
                .thenThrow(new IllegalArgumentException("La simulación no pertenece a este docente"));

        assertThrows(IllegalArgumentException.class, () -> service.exportar(SIM, DOCENTE));
        verifyNoInteractions(casoRepository, estadoRepository);
    }
}
