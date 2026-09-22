package com.estratego.infrastructure.persistence.adapter;

import com.estratego.application.mapper.UsuarioMapper;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.UsuarioRepository;
import com.estratego.infrastructure.persistence.entity.UsuarioEntity;
import com.estratego.infrastructure.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper mapper;

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return jpaRepository.findByCorreo(correo).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByUsuario(String usuario) {
        return jpaRepository.findByUsuario(usuario).map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Usuario> findByRol(Rol rol) {
        return jpaRepository.findByRol(rol).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    public boolean existsByUsuario(String usuario) {
        return jpaRepository.existsByUsuario(usuario);
    }

    @Override
    public boolean existsByNumeroIdentificacion(String numeroIdentificacion) {
        return jpaRepository.existsByNumeroIdentificacion(numeroIdentificacion);
    }
}