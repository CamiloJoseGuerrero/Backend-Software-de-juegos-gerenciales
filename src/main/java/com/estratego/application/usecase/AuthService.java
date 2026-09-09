package com.estratego.application.usecase;

import com.estratego.application.dto.auth.LoginRequest;
import com.estratego.application.dto.auth.LoginResponse;
import com.estratego.application.dto.auth.RegistroDocenteRequest;
import com.estratego.application.dto.auth.SesionResponse;
import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.mapper.UsuarioMapper;
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
        String correo = request.getCorreo().trim().toLowerCase(java.util.Locale.ROOT);
        if (loginAttemptService.isBlocked(correo)) {
            throw new CuentaBloqueadaException("Demasiados intentos fallidos. Intenta nuevamente en 15 minutos");
        }

        // 1. Buscar usuario por correo
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        // 2. Validar contraseña
        if (usuario == null || !passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            loginAttemptService.recordFailure(correo);
            log.warn("Intento de login fallido para correo: {}", correo);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        loginAttemptService.recordSuccess(correo);
        log.info("Login exitoso para correo: {}", correo);

        // 3. Generar JWT
        String token = jwtService.generateToken(usuario.getCorreo());

        // 4. Mapear usuario a respuesta (sin contraseña)
        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);

        // 5. Retornar respuesta
        return new LoginResponse(token, usuarioResponse);
    }

    public LoginResponse registroDocente(RegistroDocenteRequest request) {
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new UsuarioDuplicadoException("El correo ya está registrado");
        }
        if (usuarioRepository.existsByNumeroIdentificacion(request.getNumeroIdentificacion().trim())) {
            throw new UsuarioDuplicadoException("El número de identificación ya está registrado");
        }

        Usuario usuario = new Usuario(
                null,
                request.getNombre().trim(),
                correo,
                request.getNumeroIdentificacion().trim(),
                passwordEncoder.encode(request.getContrasena()),
                com.estratego.domain.model.usuario.Rol.DOCENTE
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
