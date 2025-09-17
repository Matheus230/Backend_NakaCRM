package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.InteracaoCliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nakacorp.backend.model.enums.TipoInteracao;

import java.time.LocalDateTime;
import java.util.Map;

public record InteracaoClienteResponseDto(
        Long id,
        ClienteSummaryDto cliente,
        UsuarioResponseDto usuario,
        TipoInteracao tipoInteracao,
        String descricao,
        Map<String, Object> dadosExtras,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static InteracaoClienteResponseDto fromEntity(InteracaoCliente interacao) {
        return new InteracaoClienteResponseDto(
                interacao.getId(),
                ClienteSummaryDto.fromEntity(interacao.getCliente()),
                interacao.getUsuario() != null ? UsuarioResponseDto.fromEntity(interacao.getUsuario()) : null,
                interacao.getTipoInteracao(),
                interacao.getDescricao(),
                interacao.getDadosExtras(),
                interacao.getCreatedAt()
        );
    }
}
