package com.estratego.application.usecase;

import com.estratego.application.dto.auth.CambiarContrasenaRequest;
import com.estratego.application.dto.auth.CambiarContrasenaResponse;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Usa BCrypt real (costo bajo para que sea rápido) para probar el flujo como en producción. */
@ExtendWith(MockitoExtension.class)
class CambiarContrasenaServiceTest {

    private static final String CORREO = "ana@correo.com";
    private static final String ACTUAL = "Actual-123!";
    private static final String NUEVA = "Nueva-456!";

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private CambiarContrasenaService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new CambiarContrasenaService(usuarioRepository, passwordEncoder);
        usuario = new Usuario(1L, "Ana", CORREO, "123", "ana", passwordEncoder.encode(ACTUAL), Rol.ESTUDIANTE);
    }

    @Test
    void cambiaLaContrasenaYGuardaElHashDeLaNueva() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));

        CambiarContrasenaResponse respuesta = service.cambiar(CORREO, new CambiarContrasenaRequest(ACTUAL, NUEVA));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        String hash = guardado.getValue().getContrasena();
        assertThat(hash).isNotEqualTo(NUEVA);
        assertThat(passwordEncoder.matches(NUEVA, hash)).isTrue();
        assertThat(passwordEncoder.matches(ACTUAL, hash)).isFalse();
        assertThat(respuesta.getMessage()).isEqualTo("Contraseña actualizada correctamente");
    }

    @Test
    void rechazaSiLaContrasenaActualEsIncorrecta() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.cambiar(CORREO, new CambiarContrasenaRequest("Otra-999!", NUEVA)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("La contraseña actual es incorrecta");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaSiLaNuevaEsIgualALaActual() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.cambiar(CORREO, new CambiarContrasenaRequest(ACTUAL, ACTUAL)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La nueva contraseña debe ser diferente a la actual");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaSiElUsuarioNoExiste() {
        when(usuarioRepository.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiar("nadie@correo.com", new CambiarContrasenaRequest(ACTUAL, NUEVA)))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(usuarioRepository, never()).save(any());
    }
}
