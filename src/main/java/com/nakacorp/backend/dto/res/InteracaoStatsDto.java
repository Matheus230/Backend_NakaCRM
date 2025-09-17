package com.nakacorp.backend.dto.res;

public record InteracaoStatsDto(
        long totalInteracoes,
        long emails,
        long telefones,
        long whatsapp,
        long formSubmissions,
        long siteVisits,
        long notasInternas
) {}
