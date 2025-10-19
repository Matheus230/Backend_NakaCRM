package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.enums.StatusLead;

import java.math.BigDecimal;

/**
 * DTO para representar um estágio do pipeline de vendas.
 * <p>
 * Mostra quantidade de leads e valor potencial em cada estágio do funil.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public record PipelineStatusDto(
        StatusLead status,
        long quantidade,
        BigDecimal valorPotencial,
        double percentualTotal
) {}
