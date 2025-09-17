package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.LeadOrigem;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record LeadOrigemResponseDto(
        Long id,
        Long clienteId,
        String fonteDetalhada,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        String userAgent,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static LeadOrigemResponseDto fromEntity(LeadOrigem leadOrigem) {
        return new LeadOrigemResponseDto(
                leadOrigem.getId(),
                leadOrigem.getCliente().getId(),
                leadOrigem.getFonteDetalhada(),
                leadOrigem.getUtmSource(),
                leadOrigem.getUtmMedium(),
                leadOrigem.getUtmCampaign(),
                leadOrigem.getUserAgent(),
                leadOrigem.getCreatedAt()
        );
    }
}
