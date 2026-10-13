package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdenCasosTest {

    private static Caso caso(Long id, LocalDateTime inicio) {
        Caso c = new Caso();
        c.setId(id);
        c.setFechaInicio(inicio);
        return c;
    }

    @Test
    void elPrimeroEsElDeFechaDeInicioMasTempranaYEnEmpateElCreadoAntes() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 20, 8, 0);
        Caso tarde = caso(1L, t.plusDays(3));
        Caso empateNuevo = caso(7L, t);
        Caso empateViejo = caso(4L, t);
        Caso sinId = caso(null, t);

        assertSame(empateViejo, OrdenCasos.primero(List.of(tarde, empateNuevo, sinId, empateViejo)).orElseThrow());
        assertEquals(List.of(empateViejo, empateNuevo, sinId, tarde),
                OrdenCasos.ordenados(List.of(tarde, sinId, empateNuevo, empateViejo)));
        assertTrue(OrdenCasos.primero(List.of()).isEmpty());
    }
}
