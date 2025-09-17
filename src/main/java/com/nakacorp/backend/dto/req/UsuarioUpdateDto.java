package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDto(
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Email(message = "Email deve ser válido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        TipoUsuario tipoUsuario,

        @Size(max = 255, message = "Google ID deve ter no máximo 255 caracteres")
        String googleId,

        Boolean ativo
) {}