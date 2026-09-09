package com.estratego.application.usecase;

import com.estratego.application.dto.auth.LoginRequest;
import com.estratego.application.dto.auth.LoginResponse;
import com.estratego.application.dto.auth.RegistroDocenteRequest;
import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.mapper.UsuarioMapper;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import com.estratego.infrastructure.security.JwtService;
import com.estratego.infrastructure.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "Ana", "ana@correo.com", "123", "hashed", Rol.DOCENTE);
    }

    @Test
    void loginExitosoRetornaTokenYUsuario() {
        LoginRequest request = new LoginRequest(" ANA@CORREO.COM ", "Password1!");
        UsuarioResponse usuarioResponse = new UsuarioResponse(1L, "Ana", "ana@correo.com", "123", "DOCENTE");

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Password1!", "hashed")).thenReturn(true);
        when(jwtService.generateToken("ana@correo.com")).thenReturn("token");
        when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioResponse);

        LoginResponse response = authService.login(request);

        assertEquals("token", response.getToken());
        assertEquals(usuarioResponse, response.getUsuario());
        verify(loginAttemptService).recordSuccess("ana@correo.com");
    }

    @Test
    void loginConCredencialesInvalidasRegistraFallo() {
        LoginRequest request = new LoginRequest("ana@correo.com", "incorrecta");
        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(loginAttemptService).recordFailure("ana@correo.com");
    }

    @Test
    void loginEstudianteRetornaRolEstudiante() {
        Usuario estudiante = new Usuario(2L, "Carlos", "carlos@correo.com", "456", "hashed", Rol.ESTUDIANTE);
        LoginRequest request = new LoginRequest("carlos@correo.com", "Password1!");
        UsuarioResponse usuarioResponse = new UsuarioResponse(2L, "Carlos", "carlos@correo.com", "456", "ESTUDIANTE");

        when(usuarioRepository.findByCorreo("carlos@correo.com")).thenReturn(Optional.of(estudiante));
        when(passwordEncoder.matches("Password1!", "hashed")).thenReturn(true);
        when(jwtService.generateToken("carlos@correo.com")).thenReturn("token-estudiante");
        when(usuarioMapper.toResponse(estudiante)).thenReturn(usuarioResponse);

        LoginResponse response = authService.login(request);

        assertEquals("ESTUDIANTE", response.getUsuario().getRol());
    }

    @Test
    void registroDocenteHasheaContrasenaYAsignaRol() {
        RegistroDocenteRequest request = new RegistroDocenteRequest(
                " Ana ", "ANA@CORREO.COM ", " 123 ", "Password1!"
        );
        Usuario guardado = new Usuario(1L, "Ana", "ana@correo.com", "123", "hashed", Rol.DOCENTE);
        UsuarioResponse usuarioResponse = new UsuarioResponse(1L, "Ana", "ana@correo.com", "123", "DOCENTE");

        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);
        when(jwtService.generateToken("ana@correo.com")).thenReturn("token");
        when(usuarioMapper.toResponse(guardado)).thenReturn(usuarioResponse);

        LoginResponse response = authService.registroDocente(request);

        assertEquals("token", response.getToken());
        verify(passwordEncoder).encode("Password1!");
        verify(usuarioRepository).save(argThat(saved ->
            saved.getRol() == Rol.DOCENTE
                && saved.getCorreo().equals("ana@correo.com")
                && saved.getNumeroIdentificacion().equals("123")
        ));
        }

        @Test
        void registroDocenteRechazaCorreoDuplicado() {
        RegistroDocenteRequest request = new RegistroDocenteRequest(
            "Ana", "ana@correo.com", "123", "Password1!"
        );
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        assertThrows(UsuarioDuplicadoException.class, () -> authService.registroDocente(request));
        verify(usuarioRepository).existsByCorreo("ana@correo.com");
        }

        @Test
        void registroDocenteRechazaIdentificacionDuplicada() {
        RegistroDocenteRequest request = new RegistroDocenteRequest(
            "Ana", "ana@correo.com", "123", "Password1!"
        );
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(usuarioRepository.existsByNumeroIdentificacion("123")).thenReturn(true);

        assertThrows(UsuarioDuplicadoException.class, () -> authService.registroDocente(request));
        verify(usuarioRepository).existsByNumeroIdentificacion("123");
    }
}
