package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.TipoInteracao;

import java.util.Map;

public record InteracaoClienteUpdateDto(
        TipoInteracao tipoInteracao,
        String descricao,
        Map<String, Object> dadosExtras
) {}