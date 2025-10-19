package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Email(message = "Email deve ser válido")
        @NotBlank(message = "Email é obrigatório")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @NotNull(message = "Tipo de usuário é obrigatório")
        TipoUsuario tipoUsuario,

        @Size(max = 255, message = "Google ID deve ter no máximo 255 caracteres")
        String googleId,

        Boolean ativo
) {
    public UsuarioRequestDto {
        if (ativo == null) ativo = true;
    }
}
