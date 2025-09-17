package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequestDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @Email(message = "Email deve ser válido")
        @NotBlank(message = "Email é obrigatório")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        String endereco,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Size(max = 2, message = "Estado deve ter 2 caracteres")
        String estado,

        @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
        String cep,

        @Size(max = 255, message = "Empresa deve ter no máximo 255 caracteres")
        String empresa,

        @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
        String cargo,

        @NotNull(message = "Origem do lead é obrigatória")
        OrigemLead origemLead,

        StatusLead statusLead,

        String observacoes
) {
    public ClienteRequestDto {
        if (statusLead == null) statusLead = StatusLead.NOVO;
    }
}
