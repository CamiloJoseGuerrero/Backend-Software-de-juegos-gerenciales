package com.estratego.domain.repository;

import com.estratego.domain.model.usuario.Usuario;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByCorreo(String correo);

    Usuario save(Usuario usuario);

    boolean existsByCorreo(String correo);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

}
