package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.TipoInteracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record InteracaoClienteRequestDto(
        @NotNull(message = "ID do cliente é obrigatório")
        Long clienteId,

        Long usuarioId,

        @NotNull(message = "Tipo de interação é obrigatório")
        TipoInteracao tipoInteracao,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        Map<String, Object> dadosExtras
) {}
