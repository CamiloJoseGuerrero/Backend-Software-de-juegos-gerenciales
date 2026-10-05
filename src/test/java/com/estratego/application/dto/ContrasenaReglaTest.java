package com.estratego.application.dto;

import com.estratego.application.dto.auth.CambiarContrasenaRequest;
import com.estratego.application.dto.auth.RegistroDocenteRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Registro y cambio de contraseña usan la misma regla: símbolo = no letra, no número, no espacio. */
class ContrasenaReglaTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Password1!   | true",
            "Password1~   | true",
            "Password1€   | true",
            "Password1_   | true",
            "Password1    | false",   // sin símbolo
            "'Pass word1' | false",   // el espacio no cuenta como símbolo
            "Passwordñ1   | false",   // la ñ es letra
            "pass1!       | false",   // corta y sin mayúscula
    })
    void mismaReglaEnRegistroYCambio(String clave, boolean valida) {
        boolean okCambio = validator.validate(new CambiarContrasenaRequest("actual", clave)).isEmpty();
        long erroresRegistro = validator.validate(new RegistroDocenteRequest("Doc", "doc@test.com", "123", clave))
                .stream().filter(v -> v.getPropertyPath().toString().equals("contrasena")).count();

        assertEquals(valida, okCambio, "cambiar-contrasena: " + clave);
        assertEquals(valida, erroresRegistro == 0, "registro-docente: " + clave);
    }
}
