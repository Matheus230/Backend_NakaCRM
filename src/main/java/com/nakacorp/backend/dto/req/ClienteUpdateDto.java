package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ClienteUpdateDto(
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @Email(message = "Email deve ser válido")
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

        OrigemLead origemLead,

        StatusLead statusLead,

        LocalDateTime dataPrimeiroContato,

        LocalDateTime dataUltimaInteracao,

        String observacoes
) {}
