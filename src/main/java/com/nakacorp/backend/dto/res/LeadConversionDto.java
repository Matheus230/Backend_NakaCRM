package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.enums.OrigemLead;

public record LeadConversionDto(
        OrigemLead origem,
        long totalLeads,
        long leadsConvertidos,
        double taxaConversao
) {}
