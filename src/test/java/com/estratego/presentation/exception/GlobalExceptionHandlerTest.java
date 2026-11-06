package com.estratego.presentation.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Errores del framework que antes caían en el 500 genérico y ahora tienen su código propio. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("unused")
    void metodoDeEjemplo(Long id) {
    }

    @Test
    void rutaInexistenteResponde404() {
        ResponseEntity<ApiError> r = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "api/docente/equipos"), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(r.getBody().getStatus()).isEqualTo(404);
        assertThat(r.getBody().getMessage()).contains("api/docente/equipos");
        assertThat(r.getBody().getErrores()).isNull();
    }

    @Test
    void metodoNoPermitidoResponde405ConLosMetodosPermitidos() {
        ResponseEntity<ApiError> r = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET", "PUT")), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(r.getBody().getMessage()).contains("DELETE").contains("GET, PUT");
    }

    @Test
    void tipoDeContenidoNoSoportadoResponde415() {
        ResponseEntity<ApiError> r = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)),
                null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(r.getBody().getMessage()).contains("text/plain");
    }

    @Test
    void idQueNoEsNumeroResponde400ConElCampo() throws Exception {
        MethodParameter parametro = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("metodoDeEjemplo", Long.class), 0);
        ResponseEntity<ApiError> r = handler.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", parametro, null), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getMessage()).contains("'abc'").contains("Long");
        assertThat(r.getBody().getErrores()).extracting(ApiError.ErrorCampo::campo).containsExactly("id");
    }

    @Test
    void parametroObligatorioQueFaltaResponde400ConElCampo() {
        ResponseEntity<ApiError> r = handler.handleMissingParameter(
                new MissingServletRequestParameterException("idSimulacion", "Long"), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getErrores()).extracting(ApiError.ErrorCampo::campo).containsExactly("idSimulacion");
    }

    @Test
    void cargaSinArchivoResponde400ConElCampo() {
        ResponseEntity<ApiError> r = handler.handleMissingPart(new MissingServletRequestPartException("archivo"), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getErrores()).extracting(ApiError.ErrorCampo::campo).containsExactly("archivo");
    }

    @Test
    void archivoDemasiadoGrandeResponde413() {
        ResponseEntity<ApiError> r = handler.handleMaxUploadSize(new MaxUploadSizeExceededException(1_048_576), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(r.getBody().getStatus()).isEqualTo(413);
    }
}
