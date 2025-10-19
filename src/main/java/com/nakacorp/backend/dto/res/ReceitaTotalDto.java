package com.nakacorp.backend.dto.res;

import java.math.BigDecimal;

/**
 * DTO para informações de receita total do CRM.
 * <p>
 * Consolida métricas financeiras relacionadas a vendas e receitas.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public record ReceitaTotalDto(
        BigDecimal receitaTotal,
        BigDecimal receitaMensal,
        BigDecimal receitaAnual,
        BigDecimal receitaPotencial,
        long totalVendas,
        long vendasMes,
        BigDecimal ticketMedio
) {}
