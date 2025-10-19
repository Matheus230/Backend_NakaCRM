package com.nakacorp.backend.dto.res;

import java.util.List;

/**
 * DTO principal para a tela Home do Dashboard.
 * <p>
 * Consolida todas as informações necessárias para a página inicial do CRM.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public record DashboardHomeDto(
        ReceitaTotalDto receita,
        DashboardStatsDto estatisticas,
        List<PipelineStatusDto> pipeline,
        List<ProximaInteracaoDto> proximasInteracoes,
        double taxaConversao,
        long leadsAtivos
) {}