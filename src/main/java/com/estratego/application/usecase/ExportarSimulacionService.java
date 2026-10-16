package com.estratego.application.usecase;

import com.estratego.application.dto.clasificacion.ClasificacionResponse;
import com.estratego.application.dto.clasificacion.FilaClasificacionResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.IndicadoresFinancieros;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.OrdenCasos;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Exporta los resultados de una simulación a Excel (opción C), para el docente.
 * Hojas: Clasificación, Estados por empresa (inicial y final de cada caso, con las 19 partidas,
 * los totales y los indicadores) y Decisiones (qué eligió cada empresa en cada caso).
 * Solo lee: no calcula nada que no esté ya en la clasificación o en los estados guardados.
 */
@Service
@RequiredArgsConstructor
public class ExportarSimulacionService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClasificacionService clasificacionService;
    private final CasoRepository casoRepository;
    private final EmpresaRepository empresaRepository;
    private final DecisionCasoRepository decisionCasoRepository;
    private final EstadoFinancieroEmpresaRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;

    /** Archivo listo para descargar. */
    public record Archivo(String nombre, byte[] contenido) {
    }

    @Transactional(readOnly = true)
    public Archivo exportar(Long idSimulacion, String correoDocente) {
        // También valida que la simulación exista y sea del docente
        ClasificacionResponse clasificacion = clasificacionService.paraDocente(idSimulacion, correoDocente);

        List<Caso> jugados = OrdenCasos.ordenados(casoRepository.findByIdSimulacion(idSimulacion).stream()
                .filter(c -> c.getActivadoEn() != null || c.getEstado() == EstadoCaso.ACTIVO)
                .toList());
        List<Empresa> empresas = empresaRepository.findByIdSimulacion(idSimulacion).stream()
                .sorted(Comparator.comparing(Empresa::getCodigoEmpresa, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(wb);
            hojaClasificacion(wb, estilos, clasificacion);
            hojaEstados(wb, estilos, jugados, empresas);
            hojaDecisiones(wb, estilos, jugados, empresas);
            wb.write(out);
            return new Archivo("estratego-simulacion-" + idSimulacion + ".xlsx", out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel de la simulación", e);
        }
    }

    // ---------------------------------------------------------------- hojas

    private void hojaClasificacion(Workbook wb, Estilos e, ClasificacionResponse c) {
        Sheet s = wb.createSheet("Clasificación");
        int r = 0;
        texto(s.createRow(r++), 0, c.getNombreSimulacion(), e.titulo);
        texto(s.createRow(r++), 0, (c.isDefinitiva() ? "Clasificación definitiva" : "Vista previa (la simulación no ha finalizado)")
                + " · casos considerados: " + c.getCasosConsiderados(), e.texto);
        r++;
        encabezado(s.createRow(r++), e, "Posición", "Código", "Empresa", "Utilidad acumulada", "Casos sin decisión");
        for (FilaClasificacionResponse f : c.getClasificacion()) {
            Row row = s.createRow(r++);
            numero(row, 0, f.getPosicion(), e.entero);
            texto(row, 1, f.getCodigoEmpresa(), e.texto);
            texto(row, 2, f.getNombreEmpresa(), e.texto);
            dinero(row, 3, f.getUtilidadAcumulada(), e);
            numero(row, 4, f.getCasosSinDecision(), e.entero);
        }
        anchos(s, 10, 12, 30, 20, 18);
        s.createFreezePane(0, 4);
    }

    private static final String[] PARTIDAS = {
            "Efectivo", "Cuentas por cobrar", "Inventarios", "Propiedad, planta y equipo", "Activos intangibles",
            "Cuentas por pagar", "Oblig. financieras CP", "Oblig. financieras LP", "Capital social", "Utilidades retenidas",
            "Ventas netas", "Costo de ventas", "Gastos de administración", "Gastos de ventas", "Gastos financieros",
            "Impuesto de renta", "Flujo operativo", "Flujo de inversión", "Flujo de financiación"};
    private static final String[] TOTALES = {"Activo total", "Pasivo total", "Patrimonio", "Utilidad neta"};
    private static final String[] INDICADORES = {"Razón corriente", "Prueba ácida", "Endeudamiento", "Margen neto", "ROA", "ROE"};

    private void hojaEstados(Workbook wb, Estilos e, List<Caso> casos, List<Empresa> empresas) {
        Sheet s = wb.createSheet("Estados por empresa");
        List<String> cols = new ArrayList<>(List.of("Caso", "Código", "Empresa", "Momento", "Origen"));
        cols.addAll(List.of(PARTIDAS));
        cols.addAll(List.of(TOTALES));
        cols.addAll(List.of(INDICADORES));
        encabezado(s.createRow(0), e, cols.toArray(String[]::new));

        Map<Long, Empresa> porId = empresas.stream().collect(Collectors.toMap(Empresa::getId, Function.identity()));
        int r = 1;
        for (Caso caso : casos) {
            List<EstadoFinancieroEmpresa> estados = estadoRepository.listarPorCaso(caso.getId()).stream()
                    .sorted(Comparator
                            .comparing((EstadoFinancieroEmpresa x) -> {
                                Empresa em = porId.get(x.getIdEmpresa());
                                return em != null && em.getCodigoEmpresa() != null ? em.getCodigoEmpresa() : "";
                            })
                            .thenComparing(x -> x.getMomento() == MomentoEstado.INICIAL ? 0 : 1))
                    .toList();
            for (EstadoFinancieroEmpresa est : estados) {
                Empresa em = porId.get(est.getIdEmpresa());
                Row row = s.createRow(r++);
                int c = 0;
                texto(row, c++, caso.getNombreEmpresa(), e.texto);
                texto(row, c++, em != null ? em.getCodigoEmpresa() : null, e.texto);
                texto(row, c++, em != null ? em.getNombre() : null, e.texto);
                texto(row, c++, est.getMomento() == MomentoEstado.INICIAL ? "Inicial" : "Final", e.texto);
                texto(row, c++, est.getOrigen().name().toLowerCase(Locale.ROOT), e.texto);
                EstadoFinanciero f = est.getEstado();
                for (BigDecimal v : partidas(f)) dinero(row, c++, v, e);
                dinero(row, c++, f.activoTotal(), e);
                dinero(row, c++, f.pasivoTotal(), e);
                dinero(row, c++, f.patrimonio(), e);
                dinero(row, c++, f.utilidadNeta(), e);
                IndicadoresFinancieros i = IndicadoresFinancieros.de(f);
                razon(row, c++, i.razonCorriente(), e.razon);
                razon(row, c++, i.pruebaAcida(), e.razon);
                razon(row, c++, i.endeudamiento(), e.porcentaje);
                razon(row, c++, i.margenNeto(), e.porcentaje);
                razon(row, c++, i.roa(), e.porcentaje);
                razon(row, c++, i.roe(), e.porcentaje);
            }
        }
        if (r == 1) {
            texto(s.createRow(r), 0, "Todavía no hay estados calculados (se calculan al activar y al cerrar cada caso).", e.texto);
        }
        s.setColumnWidth(0, 28 * 256);
        s.setColumnWidth(1, 11 * 256);
        s.setColumnWidth(2, 24 * 256);
        s.setColumnWidth(3, 10 * 256);
        s.setColumnWidth(4, 13 * 256);
        for (int c = 5; c < cols.size(); c++) s.setColumnWidth(c, 17 * 256);
        s.createFreezePane(5, 1);
    }

    private void hojaDecisiones(Workbook wb, Estilos e, List<Caso> casos, List<Empresa> empresas) {
        Sheet s = wb.createSheet("Decisiones");
        encabezado(s.createRow(0), e, "Caso", "Código", "Empresa", "Decidió", "Opción elegida", "Resultado", "Decidida por", "Fecha");
        Map<Long, String> nombres = new HashMap<>();
        int r = 1;
        for (Caso caso : casos) {
            Map<Long, DecisionCaso> porEmpresa = decisionCasoRepository.findByIdCaso(caso.getId()).stream()
                    .collect(Collectors.toMap(DecisionCaso::getIdEmpresa, Function.identity(), (a, b) -> a));
            Map<Long, OpcionCaso> opciones = caso.getOpciones().stream()
                    .collect(Collectors.toMap(OpcionCaso::getId, Function.identity(), (a, b) -> a));
            for (Empresa em : empresas) {
                DecisionCaso d = porEmpresa.get(em.getId());
                Row row = s.createRow(r++);
                texto(row, 0, caso.getNombreEmpresa(), e.texto);
                texto(row, 1, em.getCodigoEmpresa(), e.texto);
                texto(row, 2, em.getNombre(), e.texto);
                texto(row, 3, d != null ? "Sí" : "No", e.texto);
                if (d != null) {
                    OpcionCaso o = opciones.get(d.getIdOpcion());
                    texto(row, 4, o != null ? o.getOpcion() : null, e.texto);
                    texto(row, 5, o != null ? o.getResultado() : null, e.texto);
                    texto(row, 6, nombres.computeIfAbsent(d.getIdUsuario(), id ->
                            usuarioRepository.findById(id).map(u -> u.getNombre()).orElse("")), e.texto);
                    texto(row, 7, fecha(d.getFechaDecision()), e.texto);
                }
            }
        }
        anchos(s, 28, 11, 24, 9, 30, 36, 26, 17);
        s.createFreezePane(0, 1);
    }

    // ---------------------------------------------------------------- utilidades

    private static List<BigDecimal> partidas(EstadoFinanciero f) {
        return Arrays.asList(
                f.getEfectivo(), f.getCuentasPorCobrar(), f.getInventarios(), f.getPropiedadPlantaEquipo(),
                f.getActivosIntangibles(), f.getCuentasPorPagar(), f.getObligacionesFinancierasCortoPlazo(),
                f.getObligacionesFinancierasLargoPlazo(), f.getCapitalSocial(), f.getUtilidadesRetenidas(),
                f.getVentasNetas(), f.getCostoVentas(), f.getGastosAdministracion(), f.getGastosVentas(),
                f.getGastosFinancieros(), f.getImpuestoRenta(),
                f.getFlujoOperativo(), f.getFlujoInversion(), f.getFlujoFinanciacion());
    }

    private static String fecha(LocalDateTime t) {
        return t != null ? t.format(FECHA) : null;
    }

    private static void encabezado(Row row, Estilos e, String... titulos) {
        for (int i = 0; i < titulos.length; i++) texto(row, i, titulos[i], e.encabezado);
    }

    private static void texto(Row row, int col, String v, CellStyle estilo) {
        Cell c = row.createCell(col);
        if (v != null) c.setCellValue(v);
        c.setCellStyle(estilo);
    }

    private static void numero(Row row, int col, double v, CellStyle estilo) {
        Cell c = row.createCell(col);
        c.setCellValue(v);
        c.setCellStyle(estilo);
    }

    private static void dinero(Row row, int col, BigDecimal v, Estilos e) {
        Cell c = row.createCell(col);
        if (v != null) c.setCellValue(v.doubleValue());
        c.setCellStyle(e.dinero);
    }

    /** null (denominador 0) queda como celda vacía. */
    private static void razon(Row row, int col, BigDecimal v, CellStyle estilo) {
        Cell c = row.createCell(col);
        if (v != null) c.setCellValue(v.doubleValue());
        c.setCellStyle(estilo);
    }

    private static void anchos(Sheet s, int... caracteres) {
        for (int i = 0; i < caracteres.length; i++) s.setColumnWidth(i, caracteres[i] * 256);
    }

    /** Arial en todo el libro; montos con separador de miles (negativos entre paréntesis), razones y porcentajes. */
    private static final class Estilos {
        final CellStyle titulo, encabezado, texto, entero, dinero, razon, porcentaje;

        Estilos(Workbook wb) {
            DataFormat fmt = wb.createDataFormat();
            Font normal = wb.createFont();
            normal.setFontName("Arial");
            normal.setFontHeightInPoints((short) 10);
            Font negrita = wb.createFont();
            negrita.setFontName("Arial");
            negrita.setFontHeightInPoints((short) 10);
            negrita.setBold(true);
            negrita.setColor(IndexedColors.WHITE.getIndex());
            Font grande = wb.createFont();
            grande.setFontName("Arial");
            grande.setFontHeightInPoints((short) 13);
            grande.setBold(true);

            titulo = wb.createCellStyle();
            titulo.setFont(grande);
            encabezado = wb.createCellStyle();
            encabezado.setFont(negrita);
            encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setWrapText(true);
            encabezado.setVerticalAlignment(VerticalAlignment.CENTER);
            texto = estilo(wb, normal, null);
            entero = estilo(wb, normal, fmt.getFormat("0"));
            dinero = estilo(wb, normal, fmt.getFormat("#,##0;(#,##0);-"));
            razon = estilo(wb, normal, fmt.getFormat("0.00"));
            porcentaje = estilo(wb, normal, fmt.getFormat("0.0%"));
        }

        private static CellStyle estilo(Workbook wb, Font font, Short formato) {
            CellStyle s = wb.createCellStyle();
            s.setFont(font);
            if (formato != null) s.setDataFormat(formato);
            return s;
        }
    }
}
