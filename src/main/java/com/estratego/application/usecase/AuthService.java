package com.estratego.application.usecase;

import com.estratego.application.dto.auth.LoginRequest;
import com.estratego.application.dto.auth.LoginResponse;
import com.estratego.application.dto.auth.RegistroDocenteRequest;
import com.estratego.application.dto.auth.SesionResponse;
import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.mapper.UsuarioMapper;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import com.estratego.infrastructure.security.JwtService;
import com.estratego.infrastructure.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final LoginAttemptService loginAttemptService;

    public LoginResponse login(LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (loginAttemptService.isBlocked(correo)) {
            throw new CuentaBloqueadaException("Demasiados intentos fallidos. Intenta nuevamente en 15 minutos");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            loginAttemptService.recordFailure(correo);
            log.warn("Intento de login fallido para correo: {}", correo);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        loginAttemptService.recordSuccess(correo);
        log.info("Login exitoso para correo: {}", correo);

        String token = jwtService.generateToken(usuario.getCorreo());
        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);

        return new LoginResponse(token, usuarioResponse);
    }

    public LoginResponse registroDocente(RegistroDocenteRequest request) {
        // Normalizar TODO antes de validar y guardar
        String nombre = request.getNombre().trim();
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        String identificacion = request.getNumeroIdentificacion().trim();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new UsuarioDuplicadoException("El correo ya está registrado");
        }
        if (usuarioRepository.existsByNumeroIdentificacion(identificacion)) {
            throw new UsuarioDuplicadoException("El número de identificación ya está registrado");
        }

        Usuario usuario = new Usuario(
                null,
                nombre,                                              
                correo,                                              
                identificacion,                                      
                passwordEncoder.encode(request.getContrasena()),
                Rol.DOCENTE,
                null
        );

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(usuarioGuardado.getCorreo());
        return new LoginResponse(token, usuarioMapper.toResponse(usuarioGuardado));
    }

    public SesionResponse obtenerSesion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new InvalidCredentialsException("La sesión no es válida"));
        return new SesionResponse(usuarioMapper.toResponse(usuario));
    }
}