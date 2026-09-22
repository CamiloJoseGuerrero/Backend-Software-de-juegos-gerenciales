package com.estratego.application.usecase;

import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.mapper.UsuarioMapper;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
/*
@Service
@RequiredArgsConstructor
public class DocenteEstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public List<UsuarioResponse> listarEstudiantesDelDocente(String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        return usuarioRepository.findByRolAndDocenteId(Rol.ESTUDIANTE, docenteId).stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    public UsuarioResponse obtenerEstudianteDelDocente(Long estudianteId, String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        Usuario estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        if (estudiante.getRol() != Rol.ESTUDIANTE) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        if (!docenteId.equals(estudiante.getDocenteId())) {
            throw new IllegalArgumentException("El estudiante no pertenece a este docente");
        }

        return usuarioMapper.toResponse(estudiante);
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }
}*/