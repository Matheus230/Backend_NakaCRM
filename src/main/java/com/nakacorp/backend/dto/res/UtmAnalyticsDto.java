package com.nakacorp.backend.dto.res;

public record UtmAnalyticsDto(
        String utmSource,
        String utmCampaign,
        Long totalLeads,
        Long leadsConvertidos
) {}
