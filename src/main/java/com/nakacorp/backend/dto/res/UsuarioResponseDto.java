package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.Usuario;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nakacorp.backend.model.enums.TipoUsuario;

import java.time.LocalDateTime;

public record UsuarioResponseDto(
        Long id,
        String nome,
        String email,
        TipoUsuario tipoUsuario,
        String googleId,
        Boolean ativo,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static UsuarioResponseDto fromEntity(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario(),
                usuario.getGoogleId(),
                usuario.getAtivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}