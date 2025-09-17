package com.nakacorp.backend.dto.res;

public record DashboardStatsDto(
        long totalClientes,
        long leadsNovos,
        long leadsQualificados,
        long leadsConvertidos,
        long leadsPeridos,
        long totalProdutos,
        long produtosAtivos,
        InteracaoStatsDto interacoes
) {}

