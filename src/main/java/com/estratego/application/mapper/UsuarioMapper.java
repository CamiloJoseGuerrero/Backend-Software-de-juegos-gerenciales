package com.estratego.application.mapper;

import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.infrastructure.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);

    UsuarioResponse toResponse(UsuarioEntity entity);

    Usuario toDomain(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario usuario);

}
