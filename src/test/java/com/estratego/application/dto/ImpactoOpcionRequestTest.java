package com.estratego.application.dto;

import com.estratego.application.dto.docente.OpcionCasoRequest;
import com.estratego.domain.model.caso.TipoImpacto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** El impacto llega en el JSON de cada opción: se lee con Jackson y se valida con Bean Validation. */
class ImpactoOpcionRequestTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private OpcionCasoRequest leer(String impactoJson) throws Exception {
        return MAPPER.readValue(
                "{\"opcion\":\"Ampliar\",\"resultado\":\"Sube\",\"impacto\":" + impactoJson + "}",
                OpcionCasoRequest.class);
    }

    private Set<String> errores(OpcionCasoRequest r) {
        return validator.validate(r).stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void impactoValidoSeConvierteADominio() throws Exception {
        OpcionCasoRequest r = leer("{\"ventasNetas\":{\"tipo\":\"porcentaje\",\"valor\":15},"
                + "\"gastosVentas\":{\"tipo\":\"monto\",\"valor\":-5000}}");

        assertTrue(errores(r).isEmpty());
        var i = r.getImpacto().aDominio();
        assertEquals(TipoImpacto.PORCENTAJE, i.ventasNetas().tipo());
        assertEquals(new BigDecimal("-5000"), i.gastosVentas().valor());
        assertNull(i.costoVentas());
    }

    @Test
    void rechazaDriverDesconocido() throws Exception {
        OpcionCasoRequest r = leer("{\"ventasNeta\":{\"tipo\":\"monto\",\"valor\":10}}");

        assertTrue(errores(r).stream().anyMatch(m -> m.startsWith("El impacto solo acepta")));
    }

    @Test
    void rechazaPorcentajeMenorQueMenosCien() throws Exception {
        OpcionCasoRequest r = leer("{\"costoVentas\":{\"tipo\":\"porcentaje\",\"valor\":-150}}");

        assertTrue(errores(r).contains("Un impacto en porcentaje no puede ser menor que -100"));
    }

    @Test
    void montoPuedeSerMenorQueMenosCien() throws Exception {
        OpcionCasoRequest r = leer("{\"costoVentas\":{\"tipo\":\"monto\",\"valor\":-150000}}");

        assertTrue(errores(r).isEmpty());
    }

    @Test
    void rechazaDriverSinTipo() throws Exception {
        OpcionCasoRequest r = leer("{\"impuestoRenta\":{\"valor\":10}}");

        assertTrue(errores(r).contains("Cada impacto debe indicar su tipo: 'porcentaje' o 'monto'"));
    }

    @Test
    void impactoVacioQuedaEnNull() throws Exception {
        assertNull(leer("{}").getImpacto().aDominio());
    }
}
