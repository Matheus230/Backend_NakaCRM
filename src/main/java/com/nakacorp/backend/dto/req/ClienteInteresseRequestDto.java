package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.NivelInteresse;
import jakarta.validation.constraints.NotNull;

public record ClienteInteresseRequestDto(
        @NotNull(message = "ID do cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "ID do produto é obrigatório")
        Long produtoId,

        NivelInteresse nivelInteresse,

        String observacoes
) {
    public ClienteInteresseRequestDto {
        if (nivelInteresse == null) nivelInteresse = NivelInteresse.MEDIO;
    }
}
