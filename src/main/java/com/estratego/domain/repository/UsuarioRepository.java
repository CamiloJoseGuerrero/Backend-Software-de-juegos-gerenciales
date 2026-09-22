package com.estratego.domain.repository;

import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByUsuario(String usuario);

    List<Usuario> findAll();

    List<Usuario> findByRol(Rol rol);

    Usuario save(Usuario usuario);

    void deleteById(Long id);

    boolean existsByCorreo(String correo);

    boolean existsByUsuario(String usuario);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
}