package com.nakacorp.backend.dto.req;

import jakarta.validation.constraints.Size;

public record LeadOrigemUpdateDto(
        @Size(max = 255, message = "Fonte detalhada deve ter no máximo 255 caracteres")
        String fonteDetalhada,

        @Size(max = 100, message = "UTM Source deve ter no máximo 100 caracteres")
        String utmSource,

        @Size(max = 100, message = "UTM Medium deve ter no máximo 100 caracteres")
        String utmMedium,

        @Size(max = 100, message = "UTM Campaign deve ter no máximo 100 caracteres")
        String utmCampaign,

        String userAgent
) {}