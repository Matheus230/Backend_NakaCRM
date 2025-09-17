package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.ClienteInteresse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nakacorp.backend.model.enums.NivelInteresse;

import java.time.LocalDateTime;

public record ClienteInteresseResponseDto(
        Long id,
        ClienteSummaryDto cliente,
        ProdutoSummaryDto produto,
        NivelInteresse nivelInteresse,
        String observacoes,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static ClienteInteresseResponseDto fromEntity(ClienteInteresse interesse) {
        return new ClienteInteresseResponseDto(
                interesse.getId(),
                ClienteSummaryDto.fromEntity(interesse.getCliente()),
                ProdutoSummaryDto.fromEntity(interesse.getProduto()),
                interesse.getNivelInteresse(),
                interesse.getObservacoes(),
                interesse.getCreatedAt()
        );
    }
}
