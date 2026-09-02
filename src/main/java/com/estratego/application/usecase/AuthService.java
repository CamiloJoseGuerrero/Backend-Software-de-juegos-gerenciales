package com.estratego.application.usecase;

import com.estratego.application.dto.auth.LoginRequest;
import com.estratego.application.dto.auth.LoginResponse;
import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.mapper.UsuarioMapper;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import com.estratego.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario por correo
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        // 2. Validar contraseña
        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        // 3. Generar JWT
        String token = jwtService.generateToken(usuario.getCorreo());

        // 4. Mapear usuario a respuesta (sin contraseña)
        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);

        // 5. Retornar respuesta
        return new LoginResponse(token, usuarioResponse);
    }

}
