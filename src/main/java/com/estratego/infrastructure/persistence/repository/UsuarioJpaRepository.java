package com.estratego.infrastructure.persistence.repository;

import com.estratego.domain.model.usuario.Rol;
import com.estratego.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    Optional<UsuarioEntity> findByUsuario(String usuario);

    List<UsuarioEntity> findByRol(Rol rol);

    boolean existsByCorreo(String correo);

    boolean existsByUsuario(String usuario);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
}