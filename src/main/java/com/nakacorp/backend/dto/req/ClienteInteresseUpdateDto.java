package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.NivelInteresse;

public record ClienteInteresseUpdateDto(
        NivelInteresse nivelInteresse,
        String observacoes
) {}
