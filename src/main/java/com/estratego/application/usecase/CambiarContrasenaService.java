package com.estratego.application.usecase;

import com.estratego.application.dto.auth.CambiarContrasenaRequest;
import com.estratego.application.dto.auth.CambiarContrasenaResponse;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CambiarContrasenaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CambiarContrasenaResponse cambiar(String correoUsuario, CambiarContrasenaRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new InvalidCredentialsException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(request.getContrasenaNueva(), usuario.getContrasena())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getContrasenaNueva()));
        usuarioRepository.save(usuario);

        return new CambiarContrasenaResponse("Contraseña actualizada correctamente");
    }
}