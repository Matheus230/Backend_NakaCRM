package com.nakacorp.backend.dto.res;

import java.math.BigDecimal;

public record InteresseProdutoStatsDto(
        Long produtoId,
        String produtoNome,
        String categoria,
        BigDecimal preco,
        Long totalInteressados,
        Long interesseAlto,
        Long interesseMedio,
        Long interesseBaixo
) {}
